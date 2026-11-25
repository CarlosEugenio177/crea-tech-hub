# E-mail de reservas

O backend envia avisos pelo Resend quando recebe uma solicitação e quando a
equipe confirma, recusa ou cancela um pedido. O link individual abre o
acompanhamento. A mensagem da equipe é consultada na página do pedido.

## Configuração

O envio vem desativado. Quando houver domínio, use os valores reais nas variáveis
do processo do backend. Guarde as chaves no gerenciador de segredos do ambiente.

| Variável | Valor |
|---|---|
| `RESERVATION_EMAIL_ENABLED` | `true` depois de configurar e testar o domínio |
| `RESERVATION_EMAIL_RESEND_API_KEY` | Chave de envio do Resend para o domínio verificado |
| `RESERVATION_EMAIL_FROM` | Endereço do remetente, por exemplo `Hub <reservas@avisos.example.com>` |
| `RESERVATION_EMAIL_PUBLIC_ORIGIN` | Origem HTTPS do site, por exemplo `https://hub.example.com` |
| `RESERVATION_EMAIL_LINK_SECRET` | Segredo exclusivo desta função, com pelo menos 32 bytes aleatórios codificados em Base64 |

Sempre preserve o segredo dos links entre reinícios e instâncias. O backend
deriva um token para os e-mails e grava somente seu hash. A troca da chave
requer uma migração planejada. A fila rejeita um novo token quando já existe um
hash diferente, preservando o acesso dos links emitidos. O link da solicitação
original continua válido.

## Domínio no Cloudflare

1. Adicione ao Resend o domínio ou subdomínio usado no remetente.
2. Na zona DNS do Cloudflare, copie os registros de verificação e envio
   solicitados pelo Resend. Use os nomes, valores e prioridades do painel e
   mantenha os registros de e-mail como DNS only quando a opção estiver disponível.
3. Depois que o Resend marcar o domínio como verificado, configure as variáveis
   do backend e ative o envio no ambiente de teste.
4. Faça uma solicitação real com um endereço de teste. Confira a mensagem, o link
   individual e o estado do envio na fila administrativa.

[Guia do Resend para Cloudflare](https://resend.com/docs/guides/dns/cloudflare) ·
[Registros DNS de e-mail no Cloudflare](https://developers.cloudflare.com/dns/troubleshooting/email-issues/)

## Operação

A fila é gravada na mesma transação da solicitação ou decisão. O envio roda em
segundo plano e tenta de novo em falhas temporárias por até quatro tentativas
dentro de 12 horas. A chave de idempotência do Resend evita reenvio duplicado
nessa janela. O painel mostra o último aviso como aguardando, aceito pelo
serviço ou com falha. Aceito indica que o Resend recebeu a solicitação de envio.

A fila contém o protocolo, o tipo de aviso e o estado do envio. O corpo é
montado na hora a partir da reserva. Com a função desativada, as reservas
continuam normalmente. Solicitações e decisões registradas enquanto o envio
estava desligado não geram envios retroativos.

[API de envio do Resend](https://resend.com/docs/api-reference/emails/send-email) ·
[Idempotência do Resend](https://resend.com/docs/dashboard/emails/idempotency-keys)
