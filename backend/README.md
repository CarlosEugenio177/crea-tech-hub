# Backend do Crea Tech Hub

Fundação inicial do backend, em Java 25, Spring Boot 4.1.2 e Maven.
O Maven Wrapper fixa a versão do Maven; não é necessário instalá-lo separadamente.

## Executar

Requisito: JDK 25 no `PATH` ou em `JAVA_HOME`.

Na pasta `backend`, no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```sh
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
Os testes iniciam o servidor em porta aleatória e verificam a saúde da aplicação
e a restrição das demais rotas.

## Estrutura e convenções

A [CI do backend](../docs/12-ci-cd.md) compila, testa e disponibiliza o JAR em pushes
e pull requests para `develop` e `main`. A hospedagem para deploy automático ainda
será definida.

- `br.com.mutua.createchhub`: aplicação Spring Boot.
- `config`: configuração técnica, incluindo a política inicial de acesso.
- `src/main/resources/application.yml`: configuração comum; a porta aceita `SERVER_PORT`.
- `src/test/java`: testes da aplicação.

O backend será uma aplicação única, organizada por funcionalidade conforme cada
história for implementada: usuários, espaços, reservas e bloqueios. Cada funcionalidade
terá seus controllers, serviços, DTOs e persistência quando necessários. Os endpoints
de negócio seguirão o prefixo `/api/v1`.

Nesta etapa, apenas `GET /actuator/health` é público. As demais rotas são negadas;
não existe usuário padrão, formulário de login ou autenticação implementada.
A proteção CSRF continua habilitada. A definição da autenticação virá com CTH-12/CTH-7.

## Limite desta entrega

Esta base inicia sem banco de dados ou serviços externos. Login, cadastro, migrações,
catálogo, reservas, aprovação, notificações e frontend serão implementados nas próximas
etapas. Nenhuma história funcional está concluída por esta fundação.

Decisões de reserva já alinhadas para a implementação futura:

- Toda solicitação passa pela análise da equipe.
- Solicitações pendentes e reservas confirmadas bloqueiam o recurso no intervalo solicitado.
- A pendência não expira automaticamente; a decisão é manual.
- O solicitante informa os horários de início e fim, com precisão de minutos.
- O fim de um intervalo pode coincidir com o início do próximo.
- A antecedência mínima para solicitar ainda será definida.

As histórias em `../histories/` e seu plano de implementação são a referência atual
do escopo. O frontend ocupará uma pasta própria no monorepo quando seu trabalho começar.

Referências técnicas: [Spring Boot](https://docs.spring.io/spring-boot/),
[Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html) e
[Spring Security](https://docs.spring.io/spring-boot/reference/web/spring-security.html).
