# Guía JWT para MS-ORDERS

## 1. Requisitos
- Python 3
- PyJWT con soporte criptográfico
- Llave privada RSA de desarrollo compatible con dev-public.pem

## 2. Instalar PyJWT
Desde la raíz del proyecto:

    python3 -m venv .venv
    source .venv/bin/activate
    pip install "PyJWT[crypto]==2.15.1"

## 3. Configurar la llave privada
Solicitar al responsable del equipo la llave privada exclusiva para demostraciones.
Guardarla fuera del repositorio y ejecutar:

    export JWT_PRIVATE_KEY_PATH="$HOME/rally-jwt-keys/private.pem"

Nunca subir la llave privada a GitHub.

## 4. Generar un token
Desde la raíz del proyecto:

    python scripts/generate_dev_token.py --user-id 2

El token tendrá una duración predeterminada de 60 minutos.

## 5. Utilizar el token en Postman
- Abrir la petición.
- Entrar a Authorization.
- Seleccionar Bearer Token.
- Pegar el JWT generado.
- Enviar la petición.

## 6. Endpoints protegidos
- GET /api/orders/{id}
- POST /api/orders/{id}/payment
- PATCH /api/orders/{id}/cancel
- PATCH /api/orders/cancel

## 7. Consideraciones
- Utilizar únicamente datos de prueba autorizados.
- El usuario del token debe corresponder al propietario de la orden.
- No compartir tokens vigentes ni llaves privadas en GitHub.
- Las llaves de demostración no deben utilizarse en producción.
