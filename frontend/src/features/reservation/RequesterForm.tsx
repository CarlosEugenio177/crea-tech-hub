import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button } from '../../shared/ui/Button.tsx'
import { CheckboxField } from '../../shared/ui/CheckboxField.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextAreaField } from '../../shared/ui/TextAreaField.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import {
  PROFILE_LABELS,
  requesterSchema,
  type Requester,
} from './requesterSchema.ts'
import { RESERVE_TIME } from './reservationRoutes.ts'
import { StepActions } from './StepActions.tsx'

type RequesterFormProps = {
  requester?: Requester
  onSubmit: (requester: Requester) => void
}

export function RequesterForm({ requester, onSubmit }: RequesterFormProps) {
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(requesterSchema),
    defaultValues: requester,
  })
  const { errors } = formState

  return (
    <FormCard onSubmit={handleSubmit(onSubmit)}>
      <SelectField
        label="Perfil"
        error={errors.profile?.message}
        {...register('profile')}
      >
        <option value="">Selecione</option>
        {Object.entries(PROFILE_LABELS).map(([profile, label]) => (
          <option key={profile} value={profile}>
            {label}
          </option>
        ))}
      </SelectField>
      <TextField
        label="Nome completo"
        autoComplete="name"
        error={errors.fullName?.message}
        {...register('fullName')}
      />
      <TextField
        label="ID ou cartão da Mútua (opcional)"
        hint="Para mutualistas. A conferência é feita pela equipe."
        error={errors.mutuaId?.message}
        {...register('mutuaId')}
      />
      <TextField
        label="WhatsApp"
        type="tel"
        autoComplete="tel-national"
        placeholder="(00) 00000-0000"
        error={errors.whatsapp?.message}
        {...register('whatsapp')}
      />
      <TextField
        label="E-mail"
        type="email"
        autoComplete="email"
        error={errors.email?.message}
        {...register('email')}
      />
      <CheckboxField
        label="A reserva é para uso de outras pessoas"
        {...register('onBehalfOfOthers')}
      />
      <TextAreaField
        label="Motivo da reserva"
        rows={3}
        hint="Obrigatório para o público externo e para reservas em nome de terceiros."
        error={errors.reason?.message}
        {...register('reason')}
      />
      <TextAreaField
        label="Observação (opcional)"
        rows={3}
        error={errors.notes?.message}
        {...register('notes')}
      />
      <Notice tone="info">
        Seus dados identificam esta solicitação e permitem o contato da equipe.
        Não criam conta nem comprovam vínculo com o Crea ou a Mútua.
      </Notice>
      <CheckboxField
        label="Li e aceito as regras de uso do Hub"
        error={errors.rulesAccepted?.message}
        {...register('rulesAccepted')}
      />
      <StepActions backTo={RESERVE_TIME} backLabel="Voltar ao horário">
        <Button type="submit">Revisar solicitação</Button>
      </StepActions>
    </FormCard>
  )
}
