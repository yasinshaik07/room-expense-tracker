\# Room Expense Tracker



A full-stack Room Expense Tracker application built with Java and Spring Boot, integrated with a complete DevOps workflow.



The application helps roommates manage shared expenses, members, payments, balances, and settlement history.



\## Live Application



https://room-expense-tracker-mzm9.onrender.com



\## Features



\- Create and manage rooms

\- Join using a room code

\- Add room members

\- Track shared expenses

\- Split expenses between members

\- Calculate member balances

\- Track payments and settlements

\- View payment history

\- View expense history

\- Restore deleted expenses

\- Room-wise data management

\- Persistent cloud database storage



\## Tech Stack



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

\- Docker

\- GitHub Container Registry (GHCR)

\- Render

\- Kubernetes

\- Terraform

\- Spring Boot Actuator

\- Prometheus

\- Grafana



\## DevOps Architecture



```text

Developer

&#x20;   |

&#x20;   v

Git \& GitHub

&#x20;   |

&#x20;   v

GitHub Actions CI/CD

&#x20;   |

&#x20;   +---- Maven Build

&#x20;   |

&#x20;   +---- Docker Build

&#x20;   |

&#x20;   v

GitHub Container Registry

&#x20;   |

&#x20;   +----------------------+

&#x20;   |                      |

&#x20;   v                      v

Render Deployment      Kubernetes

&#x20;   |                      |

&#x20;   v                      v

Spring Boot App        Docker Container

&#x20;   |

&#x20;   v

Aiven MySQL

&#x20;   |

&#x20;   v

Spring Boot Actuator

&#x20;   |

&#x20;   v

Prometheus

&#x20;   |

&#x20;   v

Grafana Dashboard

```



\## CI/CD Pipeline



GitHub Actions automatically runs the CI/CD workflow when code is pushed to the `main` branch.



```text

Code Push

&#x20;   |

&#x20;   v

GitHub Actions

&#x20;   |

&#x20;   v

Maven Build

&#x20;   |

&#x20;   v

Docker Build

&#x20;   |

&#x20;   v

GitHub Container Registry

&#x20;   |

&#x20;   v

Deployment

```



\## Docker



The application is containerized using Docker.



Docker image:



```text

ghcr.io/yasinshaik07/room-expense-tracker:latest

```



\## Kubernetes



Kubernetes configuration is available in:



```text

k8s-deployment.yml

```



The project uses:



\- Deployment

\- Pods

\- LoadBalancer Service

\- Kubernetes Secrets

\- Liveness Probe

\- Readiness Probe

\- GHCR Docker image



Useful commands:



```bash

kubectl apply -f k8s-deployment.yml

kubectl get pods

kubectl get svc

```



\## Kubernetes Secrets



Database credentials are supplied through environment variables:



```text

DB\_URL

DB\_USERNAME

DB\_PASSWORD

```



Sensitive database credentials are not stored directly in the Kubernetes deployment configuration.



\## Terraform



Terraform is used for Infrastructure as Code (IaC).



Terraform configuration:



```text

terraform/

```



Useful commands:



```bash

terraform init

terraform validate

terraform plan

terraform apply

```



Terraform manages the Kubernetes `roomapp` namespace.



\## Monitoring



Monitoring architecture:



```text

Spring Boot

&#x20;   |

&#x20;   v

Actuator

&#x20;   |

&#x20;   v

Prometheus

&#x20;   |

&#x20;   v

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



Example metric:



```text

process\_cpu\_usage

```



Metrics include:



\- JVM metrics

\- CPU usage

\- Memory usage

\- HTTP request metrics

\- Database connection metrics

\- Application uptime



\## Build the Application



Windows:



```powershell

.\\mvnw clean package -DskipTests

```



\## Run Locally



```powershell

.\\mvnw spring-boot:run

```



Application:



```text

http://localhost:8080

```



\## Health Check



```text

http://localhost:8080/actuator/health

```



Expected status:



```json

{

&#x20; "status": "UP"

}

```



\## Project Structure



```text

roomapp/

├── .github/

│   └── workflows/

├── src/

├── terraform/

│   ├── main.tf

│   └── .terraform.lock.hcl

├── Dockerfile

├── k8s-deployment.yml

├── prometheus.yml

├── pom.xml

├── mvnw

├── mvnw.cmd

└── README.md

```



\## Author



\*\*Yasin Shaik\*\*



GitHub: https://github.com/yasinshaik07



\## Repository



https://github.com/yasinshaik07/room-expense-tracker

