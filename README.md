# Web-Application-Development-Lab-09

## NGUYEN VIET THAO - ITCSIU23058 

## LAB 9: SPRING SECURITY &amp; JWT AUTHENTICATION

TESTING AUTHENTICATION: 

- 7.1 Test Registration

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
}

Expected: 201 Created
{
    "id": 4,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "Test User",
    "role": "USER",
    "isActive": true,
    "createdAt": "2024-11-03T10:00:00"
}
```
![image](./securecustomerapi/output/testingAuth/test7.1.png)

- 7.2 Test Login

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "username": "admin",
    "password": "password123"
}

Expected: 200 OK
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@example.com",
    "role": "ADMIN"
}
```
![image](./securecustomerapi/output/testingAuth/test7.2.png)

- 7.3 Test Protected Endpoint (Without Token)

```
GET http://localhost:8080/api/customers

Expected: 401 Unauthorized
{
    "timestamp": "2024-11-03T10:05:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Authentication required. Please provide valid JWT token.",
    "path": "/api/customers"
}
```
![image](./securecustomerapi/output/testingAuth/test7.3.png)

- 7.4 Test Protected Endpoint (With Token)

```
GET http://localhost:8080/api/customers
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

Expected: 200 OK
[
    {
        "id": 1,
        "customerCode": "C001",
        "fullName": "John Doe",
        ...
    }
]
```
![image](./securecustomerapi/output/testingAuth/test7.4.png)

- 7.5 Test Authorization (USER trying to DELETE)
```
DELETE http://localhost:8080/api/customers/1
Authorization: Bearer <USER_TOKEN>

Expected: 403 Forbidden
{
    "timestamp": "2024-11-03T10:10:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Access denied. Insufficient permissions.",
    "path": "/api/customers/1"
}
```
![image](./securecustomerapi/output/testingAuth/test7.5.png)

- 7.6 Test Authorization (ADMIN can DELETE)
```
DELETE http://localhost:8080/api/customers/1
Authorization: Bearer <ADMIN_TOKEN>

Expected: 200 OK
{
    "message": "Customer deleted successfully"
}
```
![image](./securecustomerapi/output/testingAuth/test7.6.png)