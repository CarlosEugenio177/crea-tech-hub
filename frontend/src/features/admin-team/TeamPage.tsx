import { useState } from 'react'
import { useOutletContext } from 'react-router'
import type { components } from '../../shared/api/schema'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { ListPage } from '../../shared/ui/ListPage.tsx'
import { PagedList } from '../../shared/ui/PagedList.tsx'
import { usePermissionOptions, useTeam } from './teamApi.ts'
import { TeamListItem } from './TeamListItem.tsx'
import { ADMIN_TEAM_AUDIT, ADMIN_TEAM_NEW } from './teamRoutes.ts'

type AdminAccount = components['schemas']['AdminAccount']

export function TeamPage() {
  const account = useOutletContext<AdminAccount>()
  const [page, setPage] = useState(0)
  const team = useTeam(page)
  const options = usePermissionOptions()

  return (
    <ListPage
      eyebrow="Administração · Equipe"
      title="Equipe"
      description="Contas administrativas, permissões e situação de acesso."
      actions={
        <>
          <ButtonLink variant="secondary" to={ADMIN_TEAM_AUDIT}>
            Histórico de acesso
          </ButtonLink>
          <ButtonLink to={ADMIN_TEAM_NEW}>Novo funcionário</ButtonLink>
        </>
      }
    >
      <PagedList
        data={team.data}
        error={team.error}
        page={page}
        onPageChange={setPage}
        emptyText="Nenhuma conta administrativa encontrada."
      >
        {(member) => (
          <TeamListItem
            key={member.id}
            member={member}
            options={options.data ?? []}
            isSelf={member.id === account.id}
          />
        )}
      </PagedList>
    </ListPage>
  )
}
