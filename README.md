# serverless-apis

Personal REST APIs and web services built with Java 21 + Spring Boot 3, hosted on GCP Cloud Run.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 LTS (virtual threads, records, pattern matching) |
| Framework | Spring Boot 3.3.6 |
| Database | MongoDB Atlas M0 (free tier) |
| Hosting | GCP Cloud Run (serverless, scales to zero) |
| Container registry | GCP Artifact Registry |
| Secrets | GCP Secret Manager |
| CI/CD | GitHub Actions |
| API docs | SpringDoc / Swagger UI (`/swagger-ui.html`) |

---

## Local Development

### 1. Install Java 21

SDKMAN is already installed. Run:

```bash
sdk install java 21.0.5-zulu
sdk use java 21.0.5-zulu
java -version   # should show 21
```

To make Java 21 the default across terminals:

```bash
sdk default java 21.0.5-zulu
```

### 2. Start local MongoDB

```bash
docker compose up -d
```

This starts MongoDB 7 on `localhost:27017`. Data is persisted in a Docker volume (`mongo_data`).

### 3. Run the app

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.

| URL | Description |
|---|---|
| `http://localhost:8080/api/v1/ping` | Health check endpoint |
| `http://localhost:8080/swagger-ui.html` | Swagger UI (all endpoints) |
| `http://localhost:8080/api-docs` | Raw OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Spring Actuator health |

### 4. Run tests

```bash
./mvnw test
```

Tests use `@WebMvcTest` and do not require a running MongoDB instance.

### Local config

`src/main/resources/application-local.properties` is gitignored and can be used to override settings locally (e.g. a different MongoDB URI). The app defaults to `mongodb://localhost:27017` if `MONGODB_URI` is not set, so no extra config is needed for local dev with Docker Compose.

---

## GCP Setup (one-time)

Do this once when setting up a new GCP project or on a new machine. All commands use the `gcloud` CLI.

### Prerequisites

- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) installed and authenticated (`gcloud auth login`)
- A GCP project created in the console

```bash
export PROJECT_ID=your-gcp-project-id   # set this once, used in all commands below
gcloud config set project $PROJECT_ID
```

---

### Step 1 — Enable required APIs

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  iam.googleapis.com \
  iamcredentials.googleapis.com \
  sts.googleapis.com \
  --project=$PROJECT_ID
```

---

### Step 2 — Create Artifact Registry repository

```bash
gcloud artifacts repositories create serverless-apis \
  --repository-format=docker \
  --location=us-central1 \
  --project=$PROJECT_ID \
  --description="Docker images for serverless-apis"
```

---

### Step 3 — Store MongoDB URI in Secret Manager

Get your connection string from [MongoDB Atlas](https://cloud.mongodb.com) → Connect → Drivers. It looks like:
`mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/`

```bash
echo -n "mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/" | \
  gcloud secrets create mongodb-uri \
    --data-file=- \
    --project=$PROJECT_ID
```

To update the secret value later:

```bash
echo -n "new-connection-string" | \
  gcloud secrets versions add mongodb-uri --data-file=- --project=$PROJECT_ID
```

---

### Step 4 — Grant Cloud Run access to the secret

Cloud Run instances run as the default compute service account. It needs permission to read secrets at runtime.

```bash
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")

gcloud secrets add-iam-policy-binding mongodb-uri \
  --project=$PROJECT_ID \
  --member="serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"
```

---

### Step 5 — Create a service account for GitHub Actions

```bash
gcloud iam service-accounts create github-actions-sa \
  --project=$PROJECT_ID \
  --display-name="GitHub Actions Deploy SA"
```

Grant it the roles needed to deploy:

```bash
SA_EMAIL="github-actions-sa@${PROJECT_ID}.iam.gserviceaccount.com"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/artifactregistry.writer"

gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member="serviceAccount:${SA_EMAIL}" \
  --role="roles/iam.serviceAccountUser"
```

---

### Step 6 — Set up Workload Identity Federation (keyless GitHub Actions auth)

This lets GitHub Actions authenticate to GCP without a JSON key file.

```bash
# Create the identity pool
gcloud iam workload-identity-pools create "github-pool" \
  --project=$PROJECT_ID \
  --location="global" \
  --display-name="GitHub Actions Pool"

# Create the OIDC provider inside the pool
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
  --project=$PROJECT_ID \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --display-name="GitHub Actions Provider" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository"
```

Allow your specific GitHub repo to impersonate the service account (replace `GITHUB_USERNAME/REPO_NAME`):

```bash
POOL_ID=$(gcloud iam workload-identity-pools describe "github-pool" \
  --project=$PROJECT_ID \
  --location="global" \
  --format="value(name)")

gcloud iam service-accounts add-iam-policy-binding $SA_EMAIL \
  --project=$PROJECT_ID \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/${POOL_ID}/attribute.repository/GITHUB_USERNAME/REPO_NAME"
```

Get the two values you need for GitHub secrets:

```bash
# WIF_PROVIDER — paste this into GitHub secrets
gcloud iam workload-identity-pools providers describe "github-provider" \
  --project=$PROJECT_ID \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --format="value(name)"

# WIF_SERVICE_ACCOUNT — paste this into GitHub secrets
echo $SA_EMAIL
```

---

## GitHub Actions Setup

In your GitHub repo → **Settings → Secrets and variables → Actions**, add these three secrets:

| Secret name | Value |
|---|---|
| `GCP_PROJECT_ID` | Your GCP project ID (e.g. `my-project-123`) |
| `WIF_PROVIDER` | Full resource name from Step 6 above (starts with `projects/...`) |
| `WIF_SERVICE_ACCOUNT` | Service account email from Step 6 (ends with `@PROJECT_ID.iam.gserviceaccount.com`) |

---

## Deployment

### Automatic (CI/CD)

Every push to `main` or `java-spring` triggers the GitHub Actions workflow:

1. Runs `mvn test`
2. Builds Docker image (multi-stage, Temurin 21)
3. Pushes to Artifact Registry
4. Deploys to Cloud Run with `MONGODB_URI` injected from Secret Manager

Pull requests run tests only — no deploy.

### Manual deploy (from local machine)

Authenticate first:

```bash
gcloud auth login
gcloud auth configure-docker us-central1-docker.pkg.dev
```

Build and push:

```bash
IMAGE="us-central1-docker.pkg.dev/${PROJECT_ID}/serverless-apis/serverless-apis"

docker build -t ${IMAGE}:latest .
docker push ${IMAGE}:latest
```

Deploy:

```bash
gcloud run deploy serverless-apis \
  --image ${IMAGE}:latest \
  --region us-central1 \
  --platform managed \
  --allow-unauthenticated \
  --min-instances 0 \
  --memory 512Mi \
  --set-secrets MONGODB_URI=mongodb-uri:latest \
  --project=$PROJECT_ID
```

---

## Project Structure

```
serverless_apis/
├── src/
│   ├── main/java/com/adithyak/serverlessapis/
│   │   ├── ServerlessApisApplication.java   # entry point
│   │   ├── config/                          # Spring config (CORS, Security, etc.)
│   │   ├── controller/                      # REST controllers
│   │   ├── service/                         # Business logic
│   │   ├── repository/                      # Spring Data MongoDB repositories
│   │   ├── model/                           # MongoDB @Document classes
│   │   └── dto/                             # Records for request/response
│   ├── main/resources/
│   │   ├── application.properties           # main config (committed)
│   │   └── application-local.properties     # local overrides (gitignored)
│   └── test/java/...                        # unit and slice tests
├── .github/workflows/deploy.yml             # CI/CD pipeline
├── Dockerfile                               # multi-stage build
├── docker-compose.yml                       # local MongoDB
└── pom.xml                                  # Maven dependencies
```

---

## Adding a New API

Typical pattern for a new resource (e.g. `Post`):

1. **Model** — `model/Post.java` with `@Document(collection = "posts")`
2. **Repository** — `repository/PostRepository.java` extending `MongoRepository<Post, String>`
3. **DTO** — `dto/PostRequest.java` as a Java record with `@NotBlank` validation
4. **Service** — `service/PostService.java` with business logic
5. **Controller** — `controller/PostController.java` with `@RestController` and Swagger annotations

Use records for DTOs:

```java
public record PostRequest(@NotBlank String title, @NotBlank String content) {}
```

---

## Cost Summary

| Service | Monthly cost |
|---|---|
| Cloud Run | ~$0 (free tier: 2M requests/month) |
| Artifact Registry | ~$0 (0.5 GB free/region) |
| Secret Manager | ~$0 (6 secret versions free/month) |
| MongoDB Atlas M0 | $0 (permanent free tier, 512 MB) |
| GitHub Actions | $0 (free for public repos, 2k min/month for private) |
| **Total** | **~$0/month** |
