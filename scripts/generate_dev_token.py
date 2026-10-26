#!/usr/bin/env python3

import argparse
import os
from datetime import datetime, timedelta, timezone
from pathlib import Path

import jwt

parser = argparse.ArgumentParser(
    description="Generador de JWT para desarrollo de MS-ORDERS"
)
parser.add_argument(
    "--user-id", required=True, type=int,
    help="ID del usuario que realizará la operación"
)
parser.add_argument(
    "--minutes", type=int, default=60,
    help="Duración del token en minutos"
)
args = parser.parse_args()

if args.user_id <= 0 or args.minutes <= 0:
    parser.error("El ID y la duración deben ser positivos")

key_path = Path(
    os.environ.get(
        "JWT_PRIVATE_KEY_PATH",
        str(Path.home() / "rally-jwt-keys/private.pem")
    )
).expanduser()

if not key_path.is_file():
    parser.error(
        "No se encontró la llave privada. "
        "Configura JWT_PRIVATE_KEY_PATH con su ubicación."
    )

now = datetime.now(timezone.utc)

payload = {
    "sub": str(args.user_id),
    "iss": "rally-java-dev",
    "iat": now,
    "exp": now + timedelta(minutes=args.minutes),
}

with key_path.open("rb") as key_file:
    token = jwt.encode(
        payload,
        key_file.read(),
        algorithm="RS256"
    )

print(token)
