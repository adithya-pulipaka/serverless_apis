# serverless-apis

Personal REST APIs and web services built with Java 24 + Spring Boot 3, hosted on GCP Cloud Run.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 24 (virtual threads, records, pattern matching) |
| Framework | Spring Boot 3.5.14 |
| Database | MongoDB Atlas M0 (free tier) |
| Hosting | GCP Cloud Run (serverless, scales to zero) |
| Container registry | GCP Artifact Registry |
| Secrets | GCP Secret Manager |
| CI/CD | GitHub Actions |
| API docs | SpringDoc / Swagger UI (`/swagger-ui.html`) |

---

## API Endpoints

All endpoints require the `X-API-Key` header (except health check and Swagger).

### Health

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/ping` | Health check — no auth required |

### Todos

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/todos` | List todos. Optional filters: `?status=PENDING\|IN_PROGRESS\|DONE`, `?priority=LOW\|MEDIUM\|HIGH` |
| POST | `/api/v1/todos` | Create a todo |
| GET | `/api/v1/todos/{id}` | Get by ID |
| PUT | `/api/v1/todos/{id}` | Full update |
| PATCH | `/api/v1/todos/{id}/status` | Update status only: `?status=DONE` |
| DELETE | `/api/v1/todos/{id}` | Delete |

**Todo fields:** `title` (required), `description`, `status` (default `PENDING`), `priority` (default `MEDIUM`), `dueDate` (ISO date: `yyyy-MM-dd`)

### Reminders

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/reminders` | List reminders. Optional filter: `?status=ACTIVE\|DISMISSED\|EXPIRED` |
| POST | `/api/v1/reminders` | Create a reminder |
| GET | `/api/v1/reminders/{id}` | Get by ID |
| PUT | `/api/v1/reminders/{id}` | Full update |
| PATCH | `/api/v1/reminders/{id}/dismiss` | Mark as dismissed |
| DELETE | `/api/v1/reminders/{id}` | Delete |

**Reminder fields:** `title` (required), `description`, `remindAt` (required, ISO instant: `2026-06-01T10:00:00Z`), `recurPattern` (`NONE` | `DAILY` | `WEEKLY` | `MONTHLY`, default `NONE`)

### Expenses

| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/expenses` | List expenses. Optional filters: `?category=FOOD`, `?from=2026-01-01`, `?to=2026-01-31` |
| GET | `/api/v1/expenses/summary` | Totals grouped by category |
| POST | `/api/v1/expenses` | Create an expense |
| GET | `/api/v1/expenses/{id}` | Get by ID |
| PUT | `/api/v1/expenses/{id}` | Full update |
| DELETE | `/api/v1/expenses/{id}` | Delete |

**Expense fields:** `amount` (required, positive), `currency` (default `USD`), `category` (required: `FOOD` | `TRANSPORT` | `ENTERTAINMENT` | `UTILITIES` | `HEALTH` | `OTHER`), `description`, `date` (required, `yyyy-MM-dd`), `tags` (list of strings)

---

## Error Contract

All errors return a consistent JSON shape:

```json
{
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Todo with id '123abc' not found",
  "timestamp": "2026-05-31T10:00:00Z",
  "path": "/api/v1/todos/123abc"
}
```

| HTTP status | `error` value | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Request body fails `@NotBlank`, `@NotNull`, `@Positive`, etc. |
| 401 | `UNAUTHORIZED` | `X-API-Key` header missing or wrong |
| 404 | `NOT_FOUND` | Resource ID does not exist |
| 500 | `INTERNAL_ERROR` | Unexpected server error |

---

## Authentication

Every protected endpoint requires:

```
X-API-Key: <your-api-key>
```

The key is stored in GCP Secret Manager as `api-key` and injected into Cloud Run at runtime.

For local development the default key is `local-dev-key` (see `application.properties`). Override it by setting `API_KEY=your-key` in `src/main/resources/application-local.properties`.

> **TODO:** Migrate to Google OAuth2 / JWT when a frontend is added.

---

## Local Development

### 1. Install Java 24

SDKMAN is already installed. Run:

```bash
sdk install java 24.0.1-zulu
sdk use java 24.0.1-zulu
java -version   # should show 24
```

To make Java 24 the default across terminals:

```bash
sdk default java 24.0.1-zulu
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
| `http://localhost:8080/swagger-ui.html` | Swagger UI — click **Authorize** and enter `local-dev-key` |
| `http://localhost:8080/api-docs` | Raw OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Spring Actuator health |

### 4. Run tests

```bash
./mvnw test
```

Tests use `@WebMvcTest` and do not require a running MongoDB instance.

### Local config

`src/main/resources/application-local.properties` is gitignored and can be used to override settings locally (e.g. a different MongoDB URI or API key). The app defaults to `mongodb://localhost:27017` and `API_KEY=local-dev-key` if env vars are not set.

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

### Step 3 — Store secrets in Secret Manager

**MongoDB URI** — get your connection string from [MongoDB Atlas](https://cloud.mongodb.com) → Connect → Drivers:

```bash
echo -n "mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/" | \
  gcloud secrets create mongodb-uri \
    --data-file=- \
    --project=$PROJECT_ID
```

**API key** — generate a strong random string (e.g. `openssl rand -hex 32`):

```bash
echo -n "your-secret-api-key" | \
  gcloud secrets create api-key \
    --data-file=- \
    --project=$PROJECT_ID
```

To update a secret value later:

```bash
echo -n "new-value" | \
  gcloud secrets versions add <secret-name> --data-file=- --project=$PROJECT_ID
```

---

### Step 4 — Grant Cloud Run access to secrets

Cloud Run instances run as the default compute service account. It needs permission to read secrets at runtime.

```bash
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format="value(projectNumber)")

for SECRET in mongodb-uri api-key; do
  gcloud secrets add-iam-policy-binding $SECRET \
    --project=$PROJECT_ID \
    --member="serviceAccount:${PROJECT_NUMBER}-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
done
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

# Create the OIDC provider inside the pool (replace GITHUB_USERNAME/REPO_NAME)
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
  --project=$PROJECT_ID \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --display-name="GitHub Actions Provider" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --attribute-condition="attribute.repository=='GITHUB_USERNAME/REPO_NAME'"
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

In your GitHub repo → **Settings → Secrets and variables → Actions**, add these secrets:

| Secret name | Value |
|---|---|
| `GCP_PROJECT_ID` | Your GCP project ID (e.g. `my-project-123`) |
| `WIF_PROVIDER` | Full resource name from Step 6 above (starts with `projects/...`) |
| `WIF_SERVICE_ACCOUNT` | Service account email from Step 6 (ends with `@PROJECT_ID.iam.gserviceaccount.com`) |

---

## Deployment

### Automatic (CI/CD)

Every push to `main` or `java-spring` triggers the GitHub Actions workflow:

1. Runs Checkstyle (code quality gate — fails fast on unused imports, long lines)
2. Runs `mvn test`
3. Builds Docker image (multi-stage, Temurin 24)
4. Pushes to Artifact Registry
5. Deploys to Cloud Run with `MONGODB_URI` and `API_KEY` injected from Secret Manager

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
  --set-secrets MONGODB_URI=mongodb-uri:latest,API_KEY=api-key:latest \
  --project=$PROJECT_ID
```

---

## Project Structure

```
serverless_apis/
├── src/
│   ├── main/java/com/adithyak/serverlessapis/
│   │   ├── ServerlessApisApplication.java   # entry point + @EnableMongoAuditing
│   │   ├── shared/
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java       # Swagger info + X-API-Key security scheme
│   │   │   │   └── WebConfig.java           # CORS placeholder (TODO)
│   │   │   ├── security/
│   │   │   │   └── ApiKeyAuthFilter.java    # X-API-Key header enforcement
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ErrorResponse.java       # standard error record
│   │   │       └── ResourceNotFoundException.java
│   │   ├── todo/                            # Todo list feature
│   │   │   ├── Todo.java
│   │   │   ├── TodoStatus.java              # PENDING | IN_PROGRESS | DONE
│   │   │   ├── TodoPriority.java            # LOW | MEDIUM | HIGH
│   │   │   ├── TodoRequest.java
│   │   │   ├── TodoRepository.java
│   │   │   ├── TodoService.java
│   │   │   └── TodoController.java          # /api/v1/todos
│   │   ├── reminder/                        # Reminders feature
│   │   │   ├── Reminder.java
│   │   │   ├── ReminderStatus.java          # ACTIVE | DISMISSED | EXPIRED
│   │   │   ├── RecurPattern.java            # NONE | DAILY | WEEKLY | MONTHLY
│   │   │   ├── ReminderRequest.java
│   │   │   ├── ReminderRepository.java
│   │   │   ├── ReminderService.java
│   │   │   └── ReminderController.java      # /api/v1/reminders
│   │   ├── expense/                         # Expense tracker feature
│   │   │   ├── Expense.java
│   │   │   ├── ExpenseCategory.java         # FOOD | TRANSPORT | ENTERTAINMENT | UTILITIES | HEALTH | OTHER
│   │   │   ├── ExpenseRequest.java
│   │   │   ├── ExpenseSummary.java          # summary DTO (category, total, count)
│   │   │   ├── ExpenseRepository.java
│   │   │   ├── ExpenseService.java
│   │   │   └── ExpenseController.java       # /api/v1/expenses
│   │   └── controller/
│   │       └── HealthController.java        # /api/v1/ping (public)
│   ├── main/resources/
│   │   ├── application.properties           # main config (committed)
│   │   └── application-local.properties     # local overrides (gitignored)
│   └── test/java/...
├── .github/workflows/deploy.yml             # CI/CD pipeline
├── checkstyle.xml                           # Checkstyle rules (unused imports, line length)
├── Dockerfile                               # multi-stage build
├── docker-compose.yml                       # local MongoDB
└── pom.xml                                  # Maven dependencies + Checkstyle plugin
```

---

## Adding a New Feature

Pattern for adding a new resource (e.g. `Note`):

1. **Model** — `note/Note.java` with `@Document(collection = "notes")`, `@CreatedDate`, `@LastModifiedDate`
2. **Enums** — any `NoteStatus.java` or similar in the `note/` package
3. **DTO** — `note/NoteRequest.java` as a Java record with `@NotBlank` / `@NotNull` validation
4. **Repository** — `note/NoteRepository.java` extending `MongoRepository<Note, String>`
5. **Service** — `note/NoteService.java` throwing `ResourceNotFoundException` when ID not found
6. **Controller** — `note/NoteController.java` with `@SecurityRequirement(name = "X-API-Key")`, `@Tag`, and `@Operation` on each endpoint

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
