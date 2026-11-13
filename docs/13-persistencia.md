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
e-mail de contato, hash de senha, perfil de acesso, indicador de conta ativa e data
de criação em UTC. O banco exige os campos obrigatórios e restringe os perfis válidos.

`UserAccount` contém o mapeamento; `AccessRole` define os perfis e
`UserAccountRepository` fornece a persistência.

| Perfil | Uso previsto |
|---|---|
| `REQUESTER` | Solicitante |
| `ADMIN` | Administrador |
| `OPERATOR` | Operador do Hub |
| `BOARD` | Diretoria |
| `STAFF` | Equipe administrativa |

Esses perfis representam acesso à aplicação. O vínculo de mutualista/profissional
do Crea será tratado separadamente quando o cadastro do solicitante for implementado.
As permissões de cada perfil serão aplicadas na etapa de autenticação/autorização.

O e-mail de contato permite repetição, conforme o CTH-7. A definição do identificador
de login e de sua unicidade ficará na etapa de autenticação. O campo `password_hash`
recebe uma senha já transformada em hash; a geração desse hash virá com o cadastro.
Nenhuma conta administrativa é criada automaticamente.

## Evolução

Após uma migração ser aplicada, mantenha o arquivo original e crie a próxima versão
para novas alterações. Flyway é o responsável pela evolução do esquema; os testes
também executam essas mesmas migrações. As futuras reservas, salas e estações terão
migrações próprias quando suas funcionalidades forem iniciadas.

Para iniciar o banco e executar a aplicação, siga o [README do backend](../backend/README.md).
