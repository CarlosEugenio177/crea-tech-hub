import { useState } from 'react'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { ListPage } from '../../shared/ui/ListPage.tsx'
import { PagedList } from '../../shared/ui/PagedList.tsx'
import { RecordCard } from '../../shared/ui/RecordCard.tsx'
import { auditActionLabel, auditDate, auditDetail } from './auditLabels.ts'
import { useAudit, useMemberName } from './teamApi.ts'
import { ADMIN_TEAM } from './teamRoutes.ts'

export function AuditPage() {
  const [page, setPage] = useState(0)
  const audit = useAudit(page)
  const nameOf = useMemberName()

  return (
    <ListPage
      eyebrow="Administração · Equipe"
      title="Histórico de acesso"
      description="Alterações de contas, permissões e senhas, da mais recente para a mais antiga."
      actions={
        <ButtonLink variant="secondary" to={ADMIN_TEAM}>
          Voltar para equipe
        </ButtonLink>
      }
    >
      <PagedList
        data={audit.data}
        error={audit.error}
        page={page}
        onPageChange={setPage}
        emptyText="Nenhuma alteração registrada."
      >
        {(event) => (
          <RecordCard key={event.id} title={auditActionLabel(event)}>
            <p>
              {auditDate(event)} · Conta: {nameOf(event.targetId)}
              {event.actorId && ` · Por: ${nameOf(event.actorId)}`}
            </p>
            <p>{auditDetail(event)}</p>
          </RecordCard>
        )}
      </PagedList>
    </ListPage>
  )
}
