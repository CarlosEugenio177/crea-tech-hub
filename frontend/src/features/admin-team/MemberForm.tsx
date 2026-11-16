import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { problemMessage } from '../../shared/api/problem'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { pickPermissions } from './permissions.ts'
import { PermissionsFieldset } from './PermissionsFieldset.tsx'
import { useCreateMember, type PermissionOption } from './teamApi.ts'

const schema = z.object({
  fullName: z
    .string()
    .trim()
    .min(1, 'Informe o nome completo.')
    .max(150, 'O nome deve ter até 150 caracteres.'),
  email: z
    .email('Informe um e-mail válido.')
    .max(254, 'O e-mail deve ter até 254 caracteres.'),
  initialPassword: z
    .string()
    .min(12, 'Use pelo menos 12 caracteres.')
    .max(128, 'Use no máximo 128 caracteres.'),
  permissions: z.array(z.string()),
})

type MemberFormProps = {
  options: PermissionOption[]
  onSaved: () => void
}

export function MemberForm({ options, onSaved }: MemberFormProps) {
  const create = useCreateMember(onSaved)
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { permissions: [] },
  })

  const submit = handleSubmit((values) =>
    create.submit({
      ...values,
      permissions: pickPermissions(options, values.permissions),
    }),
  )

  return (
    <FormCard onSubmit={submit}>
      {create.error && (
        <Notice tone="error">{problemMessage(create.error)}</Notice>
      )}
      <TextField
        label="Nome completo"
        autoComplete="off"
        error={formState.errors.fullName?.message}
        {...register('fullName')}
      />
      <TextField
        label="E-mail"
        type="email"
        autoComplete="off"
        error={formState.errors.email?.message}
        {...register('email')}
      />
      <TextField
        label="Senha inicial"
        type="password"
        autoComplete="new-password"
        hint="De 12 a 128 caracteres. Entregue por um canal privado; a troca é exigida no primeiro acesso."
        error={formState.errors.initialPassword?.message}
        {...register('initialPassword')}
      />
      <PermissionsFieldset
        options={options}
        registration={register('permissions')}
      />
      <Button type="submit" disabled={create.isPending}>
        {create.isPending ? 'Salvando…' : 'Cadastrar funcionário'}
      </Button>
    </FormCard>
  )
}
