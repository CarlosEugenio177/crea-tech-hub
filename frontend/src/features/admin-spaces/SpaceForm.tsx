import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import type { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextAreaField } from '../../shared/ui/TextAreaField.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import { spaceFormSchema } from './spaceFormSchema.ts'
import { SPACE_TYPE_LABELS } from './spaceLabels.ts'
import type { Space, SpaceRequest } from './spacesApi.ts'

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
  const { register, handleSubmit, formState } = useForm<
    z.input<typeof spaceFormSchema>,
    unknown,
    z.output<typeof spaceFormSchema>
  >({
    resolver: zodResolver(spaceFormSchema),
    defaultValues: {
      minDuration: space?.bookingDuration?.minMinutes?.toString() ?? '',
      maxDuration: space?.bookingDuration?.maxMinutes?.toString() ?? '',
      ...(space && {
        name: space.name,
        type: space.type,
        capacity: space.capacity,
        description: space.description ?? '',
        photoUrl: space.photoUrl ?? '',
      }),
    },
  })

  const linkedStation = Boolean(space?.stationPlacement)
  const submit = handleSubmit(
    ({ photoUrl, minDuration, maxDuration, ...values }) =>
      onSubmit({
        ...values,
        description: values.description || null,
        ...(photoUrl || space?.photoUrl ? { photoUrl } : {}),
        ...(!linkedStation &&
        (space?.bookingDuration || minDuration || maxDuration)
          ? {
              bookingDuration: {
                minMinutes: minDuration,
                maxMinutes: maxDuration,
              },
            }
          : {}),
      }),
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
      {!linkedStation && (
        <>
          <TextField
            label="Duração mínima (minutos)"
            type="number"
            min={1}
            max={1439}
            step={1}
            inputMode="numeric"
            placeholder="Sem mínimo"
            error={formState.errors.minDuration?.message}
            {...register('minDuration')}
          />
          <TextField
            label="Duração máxima (minutos)"
            type="number"
            min={1}
            max={1439}
            step={1}
            inputMode="numeric"
            placeholder="Sem máximo"
            error={formState.errors.maxDuration?.message}
            {...register('maxDuration')}
          />
        </>
      )}
      <TextField
        label="Link da foto (opcional)"
        type="url"
        maxLength={2048}
        hint="Use um link HTTPS direto para a imagem."
        error={formState.errors.photoUrl?.message}
        {...register('photoUrl')}
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
