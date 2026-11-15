# Equipe, permissões e recuperação

## Modelo de acesso

As roles continuam sendo `ADMIN` e `USER`. Superadministrador é a identificação
funcional de uma conta `ADMIN` com `MANAGE_ACCESS`; não é uma nova role nem um campo
booleano. Permissões são verificadas no backend, além da role.

| Permissão | O que permite |
|---|---|
| `MANAGE_ACCESS` | Cadastrar funcionários, consultar equipe e auditoria, ativar/desativar contas e conceder/remover permissões de outras contas |
| `MANAGE_SPACES` | Usar o cadastro administrativo de espaços |

As permissões são independentes: gerenciar equipe não concede implicitamente acesso
às operações de espaços. O bootstrap inicial recebe ambas. Novas funcionalidades
devem definir e verificar suas permissões explicitamente, com migração correspondente.

O cadastro de funcionário é feito pelo gestor e representa sua aprovação. Não há
inscrição administrativa pública nem solicitação de cadastro de visitantes. O fluxo
de reservas continua público, sem conta de solicitante nesta fase.

## Cadastro e primeiro acesso

O gestor informa nome, e-mail único, senha inicial de 12 a 128 caracteres e permissões.
A conta é criada como `ADMIN`, ativa e com troca de senha obrigatória. O gestor deve
conferir o e-mail do funcionário e entregar a senha inicial por um canal privado.
A API não devolve a senha nem o hash.

Depois do login inicial, o funcionário pode consultar sua sessão, obter CSRF,
alterar a senha ou sair. As demais ações ficam bloqueadas até a troca de senha.
Após alterá-la, precisa entrar novamente. A senha pessoal não fica visível ao gestor.

Uma conta `ADMIN` pode ser promovida a gestora concedendo `MANAGE_ACCESS`, ou perder
esse poder removendo essa permissão. A role e o UUID permanecem os mesmos.

## Endpoints de equipe

Todos exigem `MANAGE_ACCESS`. Escritas também exigem CSRF.

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/api/v1/admin/team` | Lista somente contas administrativas |
| GET | `/api/v1/admin/team/{id}` | Consulta uma conta administrativa |
| GET | `/api/v1/admin/team/permissions` | Opções e descrições das permissões para o frontend |
| GET | `/api/v1/admin/team/audit` | Consulta alterações de acesso, da mais recente para a mais antiga |
| POST | `/api/v1/admin/team` | Cadastra funcionário; retorna 201 e `Location` |
| PUT | `/api/v1/admin/team/{id}/permissions` | Substitui o conjunto completo de permissões |
| PATCH | `/api/v1/admin/team/{id}/status` | Ativa ou desativa, com `{"active": true}` ou `false` |

Cadastro:

```json
{
  "fullName": "Nome do funcionário",
  "email": "funcionario@example.com",
  "initialPassword": "senha inicial definida pelo gestor",
  "permissions": ["MANAGE_SPACES"]
}
```

Promoção:

```json
{"permissions": ["MANAGE_SPACES", "MANAGE_ACCESS"]}
```

As listagens usam `page` a partir de 0 e `size` de 1 a 100, padrão 20, com
`items`, `page`, `size`, `totalElements` e `totalPages`. A equipe é ordenada por nome
e UUID; a auditoria, por data e UUID em ordem decrescente.

Não há exclusão física pela API. Desativar revoga o acesso e mantém o histórico.
O gestor não pode alterar suas próprias permissões ou situação: outro gestor deve
fazer isso. Alterações que deixariam o sistema sem gestor ativo são impedidas.
Um bloqueio transacional no PostgreSQL serializa as mudanças, inclusive sob concorrência.

Cada alteração de permissões, situação ou senha atualiza a versão de segurança da
conta. As sessões anteriores deixam de autorizar a próxima requisição administrativa
e recebem 401. A versão é conferida no banco; não depende apenas do menu ou de
permissões antigas guardadas no cookie/sessão. Para receber novas permissões, entre novamente.

A auditoria registra ator, conta afetada, ação, data e alterações de permissões ou
situação. Trocas de senha registram somente a ocorrência, sem senhas, hashes ou tokens.
Os identificadores históricos não são apagados por cascata ao remover uma conta.

## Senha e recuperação por e-mail

| Método | Endpoint | Corpo e resposta |
|---|---|---|
| POST | `/api/v1/admin/auth/password` | Autenticado: `currentPassword` e `newPassword`; 204 e novo login obrigatório |
| POST | `/api/v1/admin/auth/password/forgot` | Público: `email`; 202 sem revelar se a conta existe |
| POST | `/api/v1/admin/auth/password/reset` | Público: `token` e `newPassword`; 204 |

Todos exigem CSRF, obtido no endpoint já existente. Novas senhas têm de 12 a 128
caracteres. Links expirados, reutilizados ou invalidados por mudanças no acesso
retornam 400. A recuperação por e-mail não reativa contas desativadas e não concede
permissões; o primeiro administrador ativo pode recuperar a senha como qualquer outro.

O token aleatório tem 256 bits, é armazenado apenas como hash SHA-256, vale por
15 minutos e só pode ser usado uma vez. Novas emissões substituem o token anterior;
há intervalo de um minuto por conta. Solicitação e consumo compartilham o limite
de dez chamadas por endereço IP em quinze minutos por instância, retornando 429
ao exceder. Somente o endereço remoto real do servidor é usado, sem confiar
automaticamente em cabeçalhos de proxy enviados pelo cliente.

A configuração SMTP é opcional. Defina estas variáveis no processo do backend:

- `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME` e `SPRING_MAIL_PASSWORD`.
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` e `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE`, conforme o servidor SMTP.
- `RECOVERY_MAIL_FROM`: remetente autorizado.
- `RECOVERY_URL`: endereço da futura tela de redefinição, por exemplo `https://hub.example.com/redefinir-senha`.

No devcontainer, defina-as no terminal interno antes de iniciar o backend. O `.env`
do Compose configura o banco; ele não carrega automaticamente estas variáveis no Java.
A URL deve usar HTTPS, exceto HTTP em localhost para desenvolvimento, e não pode
conter credenciais, query ou fragmento. O link usa `#token=...`; o frontend deverá
ler o fragmento e enviar o token no corpo do POST, sem colocá-lo em logs.

Sem configuração, `forgot` retorna 503 para qualquer e-mail, e a aplicação continua
funcionando. Com configuração, 202 significa solicitação aceita para processamento,
não garantia de entrega. O envio usa fila em memória com capacidade limitada e
ocorre após a transação do token; falhas são registradas sem dados sensíveis. A fila
não persiste tarefas após encerramento abrupto. O titular pode solicitar outro link
após o intervalo se não receber a mensagem. A tela de redefinição e o serviço SMTP
real ainda precisam ser conectados quando os ambientes forem definidos.

## Recuperação operacional do gestor

Se o responsável perder o e-mail ou não houver SMTP, um operador autorizado com
acesso ao servidor pode recuperar uma conta administrativa existente. Esse caminho
exige o perfil explícito `recover-admin`; não existe endpoint público equivalente.
Ele reativa a conta, restaura as permissões atuais, troca a senha, invalida sessões
e registra `OPERATIONAL_RECOVERY`, preservando a role, o UUID e os demais dados.
Não cria conta nem transforma `USER` em `ADMIN`.

Com o banco configurado, na pasta `backend`, em um terminal separado:

```powershell
$env:RECOVERY_ADMIN_EMAIL = 'gestor@example.com'
$recoverySecret = Read-Host 'Nova senha (12 a 128 caracteres)' -AsSecureString
$env:RECOVERY_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $recoverySecret).Password
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=recover-admin' '-Dspring-boot.run.arguments=--spring.main.web-application-type=none'
Remove-Item Env:RECOVERY_ADMIN_PASSWORD
Remove-Item Env:RECOVERY_ADMIN_EMAIL
```

O comando executa sem servidor HTTP. Use somente no procedimento operacional
autorizado e reinicie normalmente sem esse perfil. O perfil `bootstrap-admin`
continua apenas criando o primeiro administrador, sem sobrescrever contas existentes.

## Migração e limites da entrega

V4 adiciona permissões, versão de segurança, troca inicial de senha, auditoria e
tokens. Administradores legados recebem as duas permissões, preservando os poderes
que já tinham. Contas `USER` recebem nenhuma. Senhas e roles não são alteradas pela
migração. Novas contas só recebem as permissões explicitamente escolhidas pelo gestor.

Esta entrega contém APIs do backend. As telas de equipe, permissões, primeiro acesso
e recuperação serão implementadas na etapa do frontend.

Referências: [autorização OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
e [recuperação de senha OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html).
