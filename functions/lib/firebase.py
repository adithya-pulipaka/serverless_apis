from firebase_admin import initialize_app

app = None


def connect():
    global app
    if not app:
        print("connecting to Firebase")
        app = initialize_app()
    return app
