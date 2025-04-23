
# Case Técnico: Integração com HubSpot

Uma API RESTful desenvolvida com Java e Spring Boot para integração com a API do HubSpot. Autenticação feita via OAuth, seguindo o fluxo do HubSpot,

  

## Sumário

- [Pré-requisitos](#pré-requisitos)

- [Instalação](#instalação)

- [Executando a Aplicação](#executando-a-aplicação)

- [Documentação da API](#documentação-da-api)

- [Motivação e Escolhas](#motivação-e-escolhas)

- [Melhorias Futuras](#melhorias-futuras)

## Pré-requisitos

Antes de começar, certifique-se de ter instalado em seu ambiente:


-  **Java Development Kit (JDK):** Versão 21 ou superior. Verifique a versão com: `java -version`.

-  **IDE:** É recomendada a utilização do IntelliJ IDEA mas qualquer outra IDE capaz de lidar com aplicações Spring Boot pode ser utilizada.

  

## Instalação

1.  **Clone o repositório:**

```bash
git clone https://github.com/Wattam/.git
cd seu-repositorio
```
2.  **Abra o repositório com sua IDE de preferência.**

## Executando a Aplicação

1.  **Via IDE:**

Após aberto a IDE reconhecerá o projeto Spring Boot e automaticamente criará uma configuração de execução, basta executá-la.

2.  **Via Gradle Wrapper:**

```bash
./gradlew bootRun       (Linux/macOS)
gradlew.bat bootRun     (Windows)
```

A aplicação estará disponível, por padrão, em `http://localhost:8080`.

## Documentação da API

A seguir, estão documentados os principais endpoints da API.

---
### `GET /get-authorization-url`

-  **Descrição:** Gera e retorna uma URL de autenticação com o HubSpot.

-  **Método HTTP:**  `GET`

-  **URL:**  `/get-authorization-url`

- **Headers:**

	-  `Accept: application/json`

-  **Resposta de Sucesso (200 OK):**

```json
{
	"authorizationUrl": "https://app.hubspot.com/oauth/authorize?client_id=CLIENT_ID&redirect_uri=http://localhost&scope=crm.objects.contacts.write%20oauth%20crm.objects.contacts.read"
}
```

---

### `GET /hubspot-authentication/{code}`

-  **Descrição:** Gera tokens de acesso à API do HubSpot a partir do código de autorização.

-  **Método HTTP:**  `GET`

-  **URL:**  `/hubspot-authentication/{code}`

-  **Parâmetros de Path:**

	-  `code` (string): Código de autorização.

- **Headers:**

	-  `Accept: application/json`

-  **Resposta de Sucesso (200 OK):**

```json
{
	"access_token": "ACCESS_TOKEN",
	"expires_in": 1800
}
```

-  **Respostas de Erro:**

-  `400 Bad Request`: A API do HubSpot encontrou algo de errado na requisição.
-  `500 Internal Server Error`: Erro inesperado no servidor do HubSpot.

---

### `POST /create-contact`

-  **Descrição:** Cria um novo contato no CRM da HubSpot. Requer um access token.

-  **Método HTTP:**  `POST`

-  **URL:**  `/create-contact`

-  **Headers:**

	-  `Authorization: Bearer ACCESS_TOKEN`
	-  `Content-Type: application/json`
	-  `Accept: application/json`

-  **Corpo da Requisição:**

```json
{
	"email": "joão.pereira@gmail.com",
	"lastname": "Pereira",
	"firstname": "João"
}
```

-  **Resposta de Sucesso (201 Created):**

```json
{
	"id": "116424899397",
	"createdAt": "2025-04-23T17:48:15.257Z"
}
```

-  **Respostas de Erro:**

-  `400 Bad Request`: A API do HubSpot encontrou algo de errado na requisição.
-  `500 Internal Server Error`: Erro inesperado no servidor do HubSpot.

---

### `POST /process-contact`

-  **Descrição:** Endpoint destinado a receber eventos do HubSpot relativos a criação de contatos em sua CRM.

-  **Método HTTP:**  `POST`

-  **URL:**  `/process-contact`

-  **Headers:**
	-  `Content-Type: application/json`
	-  `Accept: application/json`

-  **Corpo da Requisição:**

```json
{
	"objectId":  123,
	"subscriptionId":  3500995,
	"appId":  11421118
}
```

-  **Resposta de Sucesso (200 OK):**

```json
{
	"objectId":  123,
	"subscriptionId":  3500995,
	"appId":  11421118
}
```

---

## Motivação e Escolhas

-  **Java 21:** Como nenhuma versão foi especificada optei pela última LTS disponível.

-  **Gradle:** Gradle foi utilizado como ferramenta de build e gerenciamento de dependências devido à familiaridade do autor e maior redigibilidade.

-  **Lombok:** Utilizado para reduzir a verbosidade do código Java, gerando automaticamente `getters`, `setters`, construtores, `equals`/`hashCode`, etc., através de anotações.

-  **Resilience4j:** Utilizado para respeitar as políticas de `rate limit` do HubSpot.

## Melhorias Futuras

O endpoint para o recebimento do webhook do HubSpot não irá funcionar pois a API não passou por `deploy`. Busquei por algumas tecnologias de `secure tunnel` mas nenhuma conseguia criar um endereço fixo que eu pudesse utilizar no HubSpot. No futuro serão investigadas maneiras de contornar esse problema.