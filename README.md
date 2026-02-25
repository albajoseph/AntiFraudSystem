# 🛡️ Anti-Fraud System

A sophisticated Spring Boot backend designed for the financial sector. This system provides a fraud detection engine that evaluates transactions in real-time using a combination of **Blacklist filtering**, **Correlation analysis**, and **Dynamic limit adaptation**.

## 🚀 Key Features

* **Multi-Tiered Access Control**: Role-based security (RBAC) ensuring only `MERCHANTS` can process transactions, while `SUPPORT` handles feedback and `ADMINISTRATORS` manage users.
* **Intelligent Fraud Detection**: 
    * **Blacklists**: Real-time checking against known suspicious IPs and stolen card numbers.
    * **Heuristics**: Detection of "Region Correlation" and "IP Correlation" within a rolling 1-hour window.
* **Dynamic Limit Engine**: Automatically adjusts transaction thresholds based on human feedback using a mathematical feedback loop.
* **Comprehensive Audit Trail**: Complete history of transactions with system results and reviewer feedback.

---

## 📈 Dynamic Limit Logic

The system is not static. It "learns" from human reviewers. When a `SUPPORT` user provides feedback, the system recalculates its internal thresholds for future transactions using the following formulas:

### Formulas
* **Increase Limit**: $$new\_limit = \lceil 0.8 \times current\_limit + 0.2 \times value\_from\_transaction \rceil$$
* **Decrease Limit**: $$new\_limit = \lceil 0.8 \times current\_limit - 0.2 \times value\_from\_transaction \rceil$$

### Threshold Updates Table
| Transaction Result | Feedback | Action on Limits |
| :--- | :--- | :--- |
| ALLOWED | MANUAL_PROCESSING | Decrease Max ALLOWED |
| ALLOWED | PROHIBITED | Decrease Max ALLOWED & Max MANUAL |
| MANUAL_PROCESSING | ALLOWED | Increase Max ALLOWED |
| MANUAL_PROCESSING | PROHIBITED | Decrease Max MANUAL |
| PROHIBITED | ALLOWED | Increase Max ALLOWED & Max MANUAL |
| PROHIBITED | MANUAL_PROCESSING | Increase Max MANUAL |

---

## 🛠️ Technical Stack

* **Language**: Java 17/23
* **Framework**: Spring Boot 3.2.0
* **Security**: Spring Security (Basic Auth, Stateless Session Management)
* **Data**: Spring Data JPA with H2 File-based persistence
* **Tools**: Gradle, Jackson (JSON processing), Commons Validator

---

## 📂 API Endpoints

### 🔐 Authentication & User Management
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/user` | Anonymous | Register a new user |
| `GET` | `/api/auth/list` | Admin, Support | List all users (Sorted by ID) |
| `PUT` | `/api/auth/access` | Admin | Lock/Unlock user accounts |
| `PUT` | `/api/auth/role` | Admin | Change user roles |
| `DELETE` | `/api/auth/user/{username}` | Admin | Delete a user |

### 💳 Anti-Fraud Operations
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/antifraud/transaction` | Merchant | Validate a transaction |
| `PUT` | `/api/antifraud/transaction` | Support | Provide feedback and update limits |
| `GET` | `/api/antifraud/history` | Support | View transaction history |
| `GET` | `/api/antifraud/history/{number}` | Support | View history for a specific card |
| `POST` | `/api/antifraud/suspicious-ip` | Support | Blacklist an IP address |
| `POST` | `/api/antifraud/stolencard` | Support | Blacklist a card number |

---

## ⚙️ Configuration

The application is configured to persist data to a local file, ensuring consistency across application restarts:
```properties
spring.datasource.url=jdbc:h2:file:../service_db
spring.jpa.hibernate.ddl-auto=update
```
## 👨‍💻 Installation & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/albajoseph/AntiFraudSystem.git
   ```
2. **Build the project**:
Use the Gradle wrapper to compile the code and handle dependencies:
```bash
./gradlew build
```
3. **Run the application**:
Start the Spring Boot application locally:
```bash
./gradlew bootRun
```
4. **Access the API**:
The server will be available at the following base URL:
```
http://localhost:28852
```
