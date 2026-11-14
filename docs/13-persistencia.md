# Persistência e migrações

O backend usa PostgreSQL, Spring Data JPA e Flyway. As versões das bibliotecas são
gerenciadas pelo Spring Boot. O PostgreSQL local e o dos testes usam a imagem 17.12.

## Responsabilidades

- Flyway aplica e registra as migrações SQL em `backend/src/main/resources/db/migration`.
- JPA mapeia as entidades e fornece os repositórios.
- Hibernate valida a compatibilidade entre entidades e esquema na inicialização.
- Testcontainers fornece bancos PostgreSQL descartáveis para os testes.

## Primeira migração

`V1__create_user_accounts.sql` cria a tabela `user_accounts` com UUID, nome completo,
e-mail da conta, hash de senha, perfil de acesso, indicador de conta ativa e data
de criação em UTC. O banco exige os campos obrigatórios e restringe os perfis válidos.

`UserAccount` contém o mapeamento; `AccessRole` define os perfis e
`UserAccountRepository` fornece a persistência.

| Perfil | Uso previsto |
|---|---|
| `ADMIN` | Administrador |
| `USER` | Conta de solicitante, reservada para a evolução com autenticação |

Esses perfis representam acesso à aplicação. O vínculo de mutualista/profissional
do Crea será tratado separadamente quando o cadastro do solicitante for implementado.
Nesta fase, a área administrativa é restrita a `ADMIN` e os solicitantes não precisam
de conta para fazer suas solicitações.

V2 normaliza e torna único o e-mail da conta de acesso. Os dados de contato dos
solicitantes serão independentes dessa tabela. V1 permanece preservada, com as
roles originais convertidas por V2. O campo `password_hash` recebe a senha já
transformada em hash. A criação do primeiro administrador exige ativação explícita
do perfil descrito em [Acesso administrativo](15-autenticacao.md).

## Evolução

Após uma migração ser aplicada, mantenha o arquivo original e crie a próxima versão
para novas alterações. Flyway é o responsável pela evolução do esquema; os testes
também executam essas mesmas migrações. As futuras reservas, salas e estações terão
migrações próprias quando suas funcionalidades forem iniciadas.

Para iniciar o banco e executar a aplicação, siga o [README do backend](../backend/README.md).
