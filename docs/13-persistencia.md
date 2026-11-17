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

V6 acrescenta vínculo à sala, número e coordenadas às estações em `spaces`, sem alterar
seus UUIDs ou atribuir posições aos cadastros antigos. Consulte [Estações e mapa](20-estacoes-e-mapa.md).

V5 cria a configuração única de funcionamento do Hub e seus dias da semana, com
padrão segunda a sexta, 08:00–18:00. Consulte [Funcionamento do Hub](19-funcionamento-do-hub.md).

`V3__create_spaces.sql` cria `spaces` para salas, mesas compartilhadas e estações
individuais, com capacidade, descrição opcional, situação ativa e data de criação.
O banco restringe os tipos, exige capacidade positiva e limita estações individuais
a uma pessoa. O cadastro administrativo está descrito em [Espaços](16-espacos.md).

Após uma migração ser aplicada, mantenha o arquivo original e crie a próxima versão
para novas alterações. Flyway é o responsável pela evolução do esquema; os testes
também executam essas mesmas migrações. Reservas e demais evoluções terão migrações
próprias quando suas funcionalidades forem iniciadas.

Para iniciar o banco e executar a aplicação, siga o [README do backend](../backend/README.md).
