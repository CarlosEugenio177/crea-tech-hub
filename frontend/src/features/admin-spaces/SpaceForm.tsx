import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextAreaField } from '../../shared/ui/TextAreaField.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { SPACE_TYPE_LABELS } from './spaceLabels.ts'
import type { Space, SpaceRequest } from './spacesApi.ts'

const schema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, 'Informe o nome do espaço.')
      .max(150, 'O nome deve ter até 150 caracteres.'),
    type: z.enum(
      ['ROOM', 'SHARED_TABLE', 'WORKSTATION'],
      'Informe o tipo do espaço.',
    ),
    capacity: z
      .number('Informe a capacidade do espaço.')
      .int('A capacidade deve ser um número inteiro.')
      .positive('A capacidade deve ser maior que zero.'),
    description: z
      .string()
      .trim()
      .max(2000, 'A descrição deve ter até 2000 caracteres.'),
  })
  .refine((space) => space.type !== 'WORKSTATION' || space.capacity === 1, {
    path: ['capacity'],
    message: 'Uma estação individual deve ter capacidade para uma pessoa.',
  })

type SpaceFormProps = {
  space?: Space
  submitLabel: string
  error: unknown
  isPending: boolean
  onSubmit: (space: SpaceRequest) => void
}

export function SpaceForm({
  space,
  submitLabel,
  error,
  isPending,
  onSubmit,
}: SpaceFormProps) {
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: space && {
      name: space.name,
      type: space.type,
      capacity: space.capacity,
      description: space.description ?? '',
    },
  })

  const submit = handleSubmit((values) =>
    onSubmit({ ...values, description: values.description || null }),
  )

  return (
    <FormCard onSubmit={submit}>
      <ProblemNotice error={error} />
      <TextField
        label="Nome"
        placeholder="Ex.: Sala de reunião"
        error={formState.errors.name?.message}
        {...register('name')}
      />
      <SelectField
        label="Tipo"
        error={formState.errors.type?.message}
        {...register('type')}
      >
        <option value="">Selecione</option>
        {Object.entries(SPACE_TYPE_LABELS).map(([type, label]) => (
          <option key={type} value={type}>
            {label}
          </option>
        ))}
      </SelectField>
      <TextField
        label="Capacidade"
        type="number"
        min={1}
        step={1}
        inputMode="numeric"
        hint="Número de pessoas. Estação individual comporta uma."
        error={formState.errors.capacity?.message}
        {...register('capacity', { valueAsNumber: true })}
      />
      <TextAreaField
        label="Descrição (opcional)"
        rows={4}
        error={formState.errors.description?.message}
        {...register('description')}
      />
      <Button type="submit" disabled={isPending}>
        {isPending ? 'Salvando…' : submitLabel}
      </Button>
    </FormCard>
  )
}
