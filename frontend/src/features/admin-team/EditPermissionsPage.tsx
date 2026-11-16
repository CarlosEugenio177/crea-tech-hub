import { useNavigate, useOutletContext, useParams } from 'react-router'
import { problemMessage } from '../../shared/api/problem'
import type { components } from '../../shared/api/schema'
import { FormPage } from '../../shared/ui/FormPage.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { PermissionsForm } from './PermissionsForm.tsx'
import { usePermissionOptions, useTeamMember } from './teamApi.ts'
import { ADMIN_TEAM } from './teamRoutes.ts'

type AdminAccount = components['schemas']['AdminAccount']

export function EditPermissionsPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const account = useOutletContext<AdminAccount>()
  const member = useTeamMember(id)
  const options = usePermissionOptions()
  const error = member.error ?? options.error

  return (
    <FormPage
      eyebrow="Administração · Equipe"
      title="Permissões"
      description="O conjunto marcado substitui as permissões atuais da conta."
      backTo={ADMIN_TEAM}
      backLabel="Voltar para equipe"
    >
      {id === account.id ? (
        <Notice tone="info">
          Você não pode alterar o próprio acesso. Peça a outro gestor.
        </Notice>
      ) : error ? (
        <Notice tone="error">{problemMessage(error)}</Notice>
      ) : member.data && options.data ? (
        <PermissionsForm
          member={member.data}
          options={options.data}
          onSaved={() => void navigate(ADMIN_TEAM)}
        />
      ) : (
        <Notice tone="info">Carregando…</Notice>
      )}
    </FormPage>
  )
}
