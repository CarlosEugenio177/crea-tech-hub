# Acesso administrativo e fluxo público

## Decisão atual

O solicitante consulta os espaços e solicita a reserva sem criar conta nesta fase.
Seus dados serão informados na própria solicitação. O login futuro de solicitantes
deverá considerar a integração com Crea/Mútua.

O login desta entrega é exclusivamente administrativo, com e-mail único e senha.
`user_accounts` representa contas de acesso; os dados dos visitantes serão tratados
no fluxo de reservas e poderão se repetir entre solicitações.

As roles são `ADMIN` e `USER`. Somente contas `ADMIN` ativas podem entrar pela
autenticação local nesta fase. O vínculo de mutualista/profissional do Crea e os
benefícios exclusivos serão informações e regras independentes dessas roles.
As ações administrativas também exigem suas permissões específicas. A gestão de
equipe, o primeiro acesso e a recuperação estão em [Equipe e permissões](17-equipe-permissoes-recuperacao.md).

## Criar o primeiro administrador

Com o banco local disponível, na pasta `backend`, use o PowerShell:

```powershell
$env:BOOTSTRAP_ADMIN_NAME = 'Nome do administrador'
$env:BOOTSTRAP_ADMIN_EMAIL = 'administrador@example.com'
$adminSecret = Read-Host 'Senha inicial (12 a 128 caracteres)' -AsSecureString
$env:BOOTSTRAP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $adminSecret).Password
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=bootstrap-admin'
```

O perfil `bootstrap-admin` cria apenas o primeiro administrador. Se já existir uma
conta `ADMIN`, preserva a conta e a senha existentes. Depois da criação, reinicie
normalmente, sem esse perfil, e remova a variável de senha do terminal:

```powershell
Remove-Item Env:BOOTSTRAP_ADMIN_PASSWORD
.\mvnw.cmd spring-boot:run
```

O perfil normal não cria contas nem gera senha automática. A senha nova é armazenada
com PBKDF2-HMAC-SHA256, salt aleatório e 600.000 iterações, identificada por
`{pbkdf2-v1}`. Hashes bcrypt legados continuam aceitos pelo verificador.

## Endpoints

| Método | Endpoint | Acesso e resposta |
|---|---|---|
| GET | `/api/v1/admin/auth/csrf` | Público; entrega token e nome do cabeçalho CSRF |
| POST | `/api/v1/admin/auth/login` | E-mail e senha; sucesso 204 |
| GET | `/api/v1/admin/auth/me` | Administrador autenticado; dados públicos da conta |
| POST | `/api/v1/admin/auth/logout` | Encerra a sessão; sucesso 204 |

O login recebe `email` e `password` como `application/x-www-form-urlencoded`.
O frontend pode enviá-los com `URLSearchParams`. O navegador mantém o cookie de sessão.
Não existe endpoint público de criação de contas administrativas.

Antes do login, faça GET no endpoint CSRF e envie seu `token` no cabeçalho indicado
por `headerName`. Preserve o cookie dessa requisição nas próximas chamadas.
Após o login, obtenha um token novo antes de operações de escrita, incluindo logout.

O Spring Security gerencia a autenticação, troca o identificador da sessão ao entrar
e invalida a sessão ao sair. O cookie é HttpOnly, SameSite=Lax, e a sessão expira após
20 minutos de inatividade. Em HTTPS, configure `SESSION_COOKIE_SECURE=true`.

Rotas sob `/api/v1/admin/**` exigem `ADMIN`, exceto obtenção de CSRF e envio de login.
As operações públicas de recuperação de senha também são exceções explícitas,
documentadas no contrato de equipe. `me` agora retorna `permissions` e
`passwordChangeRequired`, permitindo ao frontend montar o menu e orientar o primeiro acesso.
As rotas públicas de espaços e reservas serão liberadas explicitamente conforme
forem implementadas. Falhas de autenticação usam mensagem uniforme e `ProblemDetail`.

## Migração de contas existentes

V1 permanece preservada. V2 normaliza os e-mails para minúsculas e remove espaços
nas extremidades, exige unicidade e reduz as roles para `ADMIN` e `USER`.
Contas antigas `ADMIN` permanecem administrativas; as demais passam a `USER`, sem
ganhar privilégios administrativos automaticamente.

Se houver e-mails duplicados após normalização, V2 interrompe e reverte suas alterações.
As contas permanecem preservadas para resolução manual dos cadastros conflitantes.

## Próximas entregas

O cadastro público de dados, protocolo e link individual de acompanhamento/cancelamento
pertencem ao fluxo de reservas. Autenticação de solicitantes pelo Crea/Mútua será uma
evolução posterior, com o mecanismo de integração ainda a definir.
