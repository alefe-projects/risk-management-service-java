# Risk Management Service

Risk Management Service is an application built with **Java 21**, using **Spring Boot** and **MongoDB**.  
The service allows managing user risk configurations through **REST API** integration.

---

## 🚀 Technologies
- Java 21
- Spring Boot
- MongoDB

---

## 📦 Available Endpoint

### Create Risk Configuration
`POST /api/risk-configs`

#### Request Body (JSON)
```json
{
  "clientId": "example-client-id",
  "apiKey": "example-api-key",
  "apiSecret": "example-api-secret",
  "maxRisk": {"type": "percentage", "value": 30},
  "dailyRisk": {"type": "absolute", "value": 5000}
}
