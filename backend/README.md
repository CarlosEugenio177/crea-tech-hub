# Backend do Crea Tech Hub

Backend em Java 25, Spring Boot 4.1.2 e Maven, com PostgreSQL, JPA e Flyway.
O Maven Wrapper fixa a versão do Maven; não é necessário instalá-lo separadamente.

## Executar

Para execução diretamente na máquina: JDK 25 no `PATH` ou em `JAVA_HOME` e Docker
em execução. No devcontainer, basta o Docker e as ferramentas do VS Code abaixo.

### Usando o Dev Container

Como alternativa à instalação do JDK e do Maven na máquina, abra a raiz do
repositório no VS Code e selecione **Reopen in Container**. Para isso, é
necessário ter o Docker (ou Docker Desktop), o VS Code e a extensão
**Dev Containers** instalados.

Antes de reabrir no container, copie `.env.example` para `.env` na raiz e preencha
`DB_PASSWORD` com uma senha local escolhida por você. O arquivo `.env` é ignorado
pelo Git e lido pelo Compose; não contém credenciais prontas no repositório.

O devcontainer sobe o PostgreSQL junto com o Java e aguarda o banco ficar saudável.
Os dois serviços compartilham a rede do Compose; o backend usa
`jdbc:postgresql://postgres:5432/crea_tech_hub`, com a mesma senha do banco.
O acesso ao Docker para Testcontainers também é configurado automaticamente.

Dentro do terminal do container:

```sh
cd backend
./mvnw spring-boot:run
```

Não é necessário subir outro banco nem configurar `DB_URL` manualmente.
A porta `8080` é encaminhada ao computador. Na primeira criação, a imagem e as
dependências Maven serão baixadas; nas próximas execuções, o cache Maven persistirá
em um volume do Docker. O código é o mesmo repositório aberto no VS Code.
O diretório `backend/target` do container também usa volume próprio: isso evita
conflitos de permissões e artefatos entre builds no Windows e no Linux do container.
Para executar o JAR produzido ali, use o terminal do próprio container.

Se já existe um volume do PostgreSQL, use a senha escolhida na sua primeira
inicialização. Alterar `.env` não altera a senha do banco existente. Não remova o
volume para contornar esse problema: preserve os dados e ajuste a credencial.

### Usando o JDK da máquina

Na raiz do repositório, prepare o banco local no PowerShell:

```powershell
$env:DB_PASSWORD = 'senha-local-da-sua-escolha'
docker compose up -d --wait postgres
```

Mantenha a variável definida no mesmo terminal ao iniciar o backend.

Em seguida, entre na pasta `backend` e execute no Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, a partir da raiz:

```sh
export DB_PASSWORD='senha-local-da-sua-escolha'
docker compose up -d --wait postgres
cd backend
./mvnw spring-boot:run
```

A aplicação inicia na porta `8080`. Para escolher outra porta no PowerShell:

```powershell
$env:SERVER_PORT = '8081'
.\mvnw.cmd spring-boot:run
```

Consulte `GET http://localhost:8080/actuator/health`. A resposta esperada é:

```json
{"groups":["liveness","readiness"],"status":"UP"}
```

## Verificar e empacotar

```powershell
.\mvnw.cmd clean verify
java -jar target/crea-tech-hub-0.0.1-SNAPSHOT.jar
```

No Linux/macOS, use `./mvnw clean verify`.
Os testes exigem Docker e criam bancos PostgreSQL temporários com Testcontainers.
Não precisam do banco local do Compose nem de `DB_PASSWORD`.
Verificam as migrações, a persistência das contas, a saúde da aplicação e a restrição
das demais rotas.

## Banco de dados

O Flyway aplica os scripts em `src/main/resources/db/migration` na inicialização.
O Hibernate usa `ddl-auto: validate` para conferir o mapeamento das entidades.
Uma alteração de esquema deve entrar em uma nova migração; scripts já aplicados
permanecem preservados.

| Variável | Uso | Padrão local |
|---|---|---|
| `DB_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/crea_tech_hub` |
| `DB_USERNAME` | Usuário do banco | `crea` |
| `DB_PASSWORD` | Senha do banco | Definida no terminal |

O Compose fornece PostgreSQL 17.12, restrito à conexão local, com dados em volume
persistente. Para parar o banco, execute `docker compose stop postgres` na raiz.

A primeira migração cria `user_accounts`. Os detalhes da modelagem estão em
[Persistência e migrações](../docs/13-persistencia.md).

## Estrutura e convenções

A [CI do backend](../docs/12-ci-cd.md) está configurada para compilar, testar e
disponibilizar o JAR em pushes e pull requests para `develop` e `main`.
A hospedagem para deploy automático ainda será definida.

- `br.com.mutua.createchhub`: aplicação Spring Boot.
- `config`: configuração técnica, incluindo a política inicial de acesso.
- `user`: conta de usuário, perfil de acesso e repositório JPA.
- `space`: cadastro administrativo de salas, mesas compartilhadas e estações.
- `reservation`: solicitação, acompanhamento, análise e cancelamento de reservas.
- `block`: criação, consulta e cancelamento de bloqueios pontuais de espaços.
- `operatinghours`: consulta pública e configuração administrativa do funcionamento do Hub.
- `access`: equipe, permissões e auditoria de acesso.
- `recovery`: recuperação de senha por token e procedimento operacional.
- `shared.error`: tratamento centralizado de exceções e respostas de erro.
- `src/main/resources/application.yml`: configuração comum; a porta aceita `SERVER_PORT`.
- `src/test/java`: testes da aplicação.

O backend é uma aplicação única organizada por funcionalidade. Os controllers recebem
as requisições HTTP, os serviços coordenam os casos de uso e os repositórios acessam o
banco. Os endpoints de negócio usam o prefixo `/api/v1`.

O contrato de erros, com `ProblemDetail`, está descrito em
[Erros da API](../docs/14-erros-da-api.md).
O contrato OpenAPI gerado, sua atualização e o uso pelo frontend estão em
[OpenAPI](../docs/18-openapi.md). A documentação HTTP é habilitada com `OPENAPI_ENABLED=true`;
os arquivos versionados ficam em `../openapi/`.

As rotas administrativas de autenticação estão descritas em
[Acesso administrativo](../docs/15-autenticacao.md). Esse documento explica como
criar o primeiro administrador, obter CSRF, entrar, consultar a sessão e sair.

O acesso administrativo usa e-mail e senha com sessão por cookie. A gestão de equipe
e a recuperação estão em [Equipe e permissões](../docs/17-equipe-permissoes-recuperacao.md).
Cada ação administrativa também exige a permissão correspondente.
No beta, qualquer pessoa pode solicitar uma reserva pelo fluxo público, com os dados
informados no pedido. A autenticação de solicitantes via Crea/Mútua fica para uma etapa futura.

## Acompanhar uma solicitação de reserva

1. Na tela pública, a pessoa escolhe o espaço e o período, preenche os dados e revisa
   o pedido. [`ReviewPage.tsx`](../frontend/src/features/reservation/ReviewPage.tsx) chama
   [`useSubmitReservation`](../frontend/src/features/reservation/reservationApi.ts) para enviar `POST /api/v1/reservations`.
2. [`PublicReservationController.submit`](src/main/java/br/com/mutua/createchhub/reservation/PublicReservationController.java)
   recebe os dados. [`ReservationService.submit`](src/main/java/br/com/mutua/createchhub/reservation/ReservationService.java)
   valida a solicitação, aplica as regras e verifica o conflito de horário.
3. [`ReservationRepository.persistPending`](src/main/java/br/com/mutua/createchhub/reservation/ReservationRepository.java) grava
   o pedido na tabela `reservations`, criada pela
   [migração V10](src/main/resources/db/migration/V10__create_reservations.sql).
   O status começa em `PENDING` e o horário fica ocupado enquanto a equipe analisa.
4. O `201 Created` devolve um código privado. O frontend abre o comprovante usando
   esse código, que também permite acompanhar e cancelar pelo link individual.

Para testar o caminho HTTP até o PostgreSQL, execute em `backend/` com o banco de
testes configurado como em [Verificar e empacotar](#verificar-e-empacotar).

```sh
./mvnw -Dtest=ReservationApiTests test
```

No Windows, use `.\mvnw.cmd` com os mesmos argumentos. Em `frontend/`, após instalar
as dependências, rode o teste da solicitação com a API simulada.

```sh
npm test -- src/features/reservation/reservationRequest.test.tsx
```

O [plano de implementação](../histories/PLANO-DE-IMPLEMENTACAO.md) reúne as histórias
e decisões de escopo atuais.

Referências técnicas: [Spring Boot](https://docs.spring.io/spring-boot/),
[Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) e
[Spring Security](https://docs.spring.io/spring-boot/reference/web/spring-security.html).
