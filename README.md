\# 🏠 Room Expense Tracker



A full-stack \*\*Room Expense Tracker\*\* application built using Java and Spring Boot with a complete DevOps workflow.



The application helps roommates manage shared expenses, members, payments, balances, and settlement history.



\## 🚀 Live Application



https://room-expense-tracker-mzm9.onrender.com



\## ✨ Features



\- Create and manage rooms

\- Join using room code

\- Add room members

\- Track shared expenses

\- Split expenses between members

\- Calculate member balances

\- Track payments

\- Payment history

\- Expense history

\- Restore deleted expenses

\- Room-wise data management

\- Persistent database storage



\## 🛠 Tech Stack



\### Backend

\- Java 25

\- Spring Boot

\- Spring MVC

\- Spring Data JPA

\- Hibernate

\- Maven



\### Frontend

\- HTML

\- CSS

\- JavaScript

\- Thymeleaf



\### Database

\- MySQL

\- Aiven Cloud MySQL



\### DevOps

\- Git

\- GitHub

\- GitHub Actions

\- Maven

\- Docker

\- GitHub Container Registry (GHCR)

\- Render

\- Kubernetes

\- Terraform

\- Spring Boot Actuator

\- Prometheus

\- Grafana



\## 🔄 DevOps Workflow



```text

Developer

&#x20;  |

&#x20;  v

Git / GitHub

&#x20;  |

&#x20;  v

GitHub Actions

&#x20;  |

&#x20;  v

Maven Build

&#x20;  |

&#x20;  v

Docker Image

&#x20;  |

&#x20;  v

GitHub Container Registry

&#x20;  |

&#x20;  +-------------------+

&#x20;  |                   |

&#x20;  v                   v

Render              Kubernetes

&#x20;  |                   |

&#x20;  v                   v

Spring Boot App     Container

&#x20;  |

&#x20;  v

Aiven MySQL

&#x20;  |

&#x20;  v

Spring Boot Actuator

&#x20;  |

&#x20;  v

Prometheus

&#x20;  |

&#x20;  v

Grafana

```



\## 🐳 Docker



The Spring Boot application is containerized using Docker.



Docker image:



```text

ghcr.io/yasinshaik07/room-expense-tracker:latest

```



\## ☸️ Kubernetes



Kubernetes configuration:



```text

k8s-deployment.yml

```



The project uses:



\- Deployment

\- Pods

\- LoadBalancer Service

\- Kubernetes Secrets

\- Liveness and readiness monitoring



Useful commands:



```bash

kubectl apply -f k8s-deployment.yml

kubectl get pods

kubectl get svc

```



\## 🔐 Kubernetes Secrets



Database credentials are provided through environment variables:



```text

DB\_URL

DB\_USERNAME

DB\_PASSWORD

```



Sensitive database credentials are not stored directly in the Kubernetes deployment file.



\## 🏗 Terraform



Terraform is used for Infrastructure as Code (IaC).



Terraform configuration is available in:



```text

terraform/

```



Commands:



```bash

terraform init

terraform validate

terraform plan

terraform apply

```



Terraform manages the Kubernetes `roomapp` namespace.



\## 📊 Monitoring



Application monitoring is implemented using:



```text

Spring Boot Actuator

&#x20;       |

&#x20;       v

Prometheus

&#x20;       |

&#x20;       v

Grafana

```



Health endpoint:



```text

/actuator/health

```



Prometheus metrics endpoint:



```text

/actuator/prometheus

```



Prometheus configuration:



```text

prometheus.yml

```



Grafana dashboard:



```text

Room Expense Tracker Monitoring

```



Metrics include JVM, CPU, memory, HTTP request, database connection, and application metrics.



\## 🔁 CI/CD



GitHub Actions is used for CI/CD.



The workflow builds the Java application and Docker image as part of the deployment process.



```text

Code Push

&#x20;  |

&#x20;  v

GitHub Actions

&#x20;  |

&#x20;  v

Maven Build

&#x20;  |

&#x20;  v

Docker Build

&#x20;  |

&#x20;  v

Container Registry

&#x20;  |

&#x20;  v

Deployment

```



\## ▶️ Build the Project



On Windows:



```powershell

.\\mvnw clean package -DskipTests

```



\## ▶️ Run Locally



```powershell

.\\mvnw spring-boot:run

```



Application runs on:



```text

http://localhost:8080

```



\## 📁 Important Project Files



```text

roomapp/

├── .github/workflows/

├── src/

├── terraform/

├── Dockerfile

├── k8s-deployment.yml

├── prometheus.yml

├── pom.xml

├── mvnw

├── mvnw.cmd

└── README.md

```



\## 👨‍💻 Author



\*\*Yasin Shaik\*\*



GitHub: https://github.com/yasinshaik07



\## 📌 Repository



https://github.com/yasinshaik07/room-expense-tracker

