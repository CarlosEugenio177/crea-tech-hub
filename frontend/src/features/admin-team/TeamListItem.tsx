import { Button } from '../../shared/ui/Button.tsx'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { RecordCard } from '../../shared/ui/RecordCard.tsx'
import { permissionSummary } from './permissions.ts'
import {
  useChangeMemberStatus,
  type PermissionOption,
  type TeamMember,
} from './teamApi.ts'
import { teamMemberPath } from './teamRoutes.ts'

type TeamListItemProps = {
  member: TeamMember
  options: PermissionOption[]
  isSelf: boolean
}

export function TeamListItem({ member, options, isSelf }: TeamListItemProps) {
  const status = useChangeMemberStatus(member.id)

  return (
    <RecordCard
      title={member.fullName}
      active={member.active}
      error={status.error}
      actions={
        !isSelf && (
          <>
            <ButtonLink variant="secondary" to={teamMemberPath(member.id)}>
              Permissões
            </ButtonLink>
            <Button
              variant="secondary"
              disabled={status.isPending}
              onClick={() => status.change(!member.active)}
            >
              {member.active ? 'Desativar' : 'Ativar'}
            </Button>
          </>
        )
      }
    >
      <p>{member.email}</p>
      <p>{permissionSummary(options, member.permissions)}</p>
      {member.passwordChangeRequired && <p>Aguardando o primeiro acesso.</p>}
      {isSelf && <p>Sua conta: outro gestor altera o seu acesso.</p>}
    </RecordCard>
  )
}
