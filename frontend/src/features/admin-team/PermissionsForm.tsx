import { useForm } from 'react-hook-form'
import { problemMessage } from '../../shared/api/problem'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { pickPermissions } from './permissions.ts'
import { PermissionsFieldset } from './PermissionsFieldset.tsx'
import {
  useChangePermissions,
  type PermissionOption,
  type TeamMember,
} from './teamApi.ts'

type PermissionsFormProps = {
  member: TeamMember
  options: PermissionOption[]
  onSaved: () => void
}

export function PermissionsForm({
  member,
  options,
  onSaved,
}: PermissionsFormProps) {
  const change = useChangePermissions(member.id, onSaved)
  const { register, handleSubmit } = useForm<{ permissions: string[] }>({
    defaultValues: { permissions: member.permissions },
  })

  const submit = handleSubmit((values) =>
    change.submit(pickPermissions(options, values.permissions)),
  )

  return (
    <FormCard onSubmit={submit}>
      {change.error && (
        <Notice tone="error">{problemMessage(change.error)}</Notice>
      )}
      <Notice tone="info">
        {member.fullName} · {member.email}. Ao salvar, as sessões dessa conta
        são encerradas.
      </Notice>
      <PermissionsFieldset
        options={options}
        registration={register('permissions')}
      />
      <Button type="submit" disabled={change.isPending}>
        {change.isPending ? 'Salvando…' : 'Salvar permissões'}
      </Button>
    </FormCard>
  )
}
