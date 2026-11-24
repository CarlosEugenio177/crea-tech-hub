import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../shared/ui/Button.tsx'
import { FormCard } from '../../shared/ui/FormCard.tsx'
import { ProblemNotice } from '../../shared/ui/ProblemNotice.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { TextAreaField } from '../../shared/ui/TextAreaField.tsx'
import { TextField } from '../../shared/ui/TextField.tsx'
import type { Space } from '../admin-spaces/spacesApi.ts'
import { maskDate, maskTime, withMask } from '../reservation/inputMasks.ts'
import {
  BRAZILIAN_DATE,
  isCalendarDate,
  TIME_24H,
  toIsoDate,
} from '../reservation/reservationDates.ts'
import styles from './SpaceBlockForm.module.css'
import type { SpaceBlockRequest } from './spaceBlocksApi.ts'

function dateField(required: string) {
  return z
    .string()
    .min(1, required)
    .regex(BRAZILIAN_DATE, 'Informe a data no formato dd/mm/aaaa.')
    .transform(toIsoDate)
    .refine(isCalendarDate, 'Informe uma data válida.')
}

function timeField(required: string) {
  return z
    .string()
    .min(1, required)
    .regex(TIME_24H, 'Informe o horário no formato hh:mm, de 00:00 a 23:59.')
}

const schema = z
  .object({
    spaceId: z.string().min(1, 'Selecione o espaço.'),
    startDate: dateField('Informe a data de início.'),
    startTime: timeField('Informe o horário de início.'),
    endDate: dateField('Informe a data de fim.'),
    endTime: timeField('Informe o horário de fim.'),
    reason: z
      .string()
      .trim()
      .min(1, 'Informe o motivo do bloqueio.')
      .max(500, 'Use até 500 caracteres.'),
  })
  .refine(
    (values) =>
      `${values.endDate}T${values.endTime}` >
      `${values.startDate}T${values.startTime}`,
    {
      path: ['endTime'],
      message: 'O fim deve ser depois do início.',
    },
  )

type SpaceBlockFormProps = {
  spaces: Space[]
  error: unknown
  isPending: boolean
  onSubmit: (block: SpaceBlockRequest) => void
}

export function SpaceBlockForm({
  spaces,
  error,
  isPending,
  onSubmit,
}: SpaceBlockFormProps) {
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      spaceId: '',
      startDate: '',
      startTime: '',
      endDate: '',
      endTime: '',
      reason: '',
    },
  })

  const submit = handleSubmit((values) => {
    onSubmit({
      spaceId: values.spaceId,
      startsAt: `${values.startDate}T${values.startTime}`,
      endsAt: `${values.endDate}T${values.endTime}`,
      reason: values.reason,
    })
  })

  return (
    <FormCard onSubmit={submit}>
      <ProblemNotice error={error} />
      <SelectField
        label="Espaço"
        error={formState.errors.spaceId?.message}
        {...register('spaceId')}
      >
        <option value="">Selecione um espaço</option>
        {spaces.map((space) => (
          <option key={space.id} value={space.id}>
            {space.active ? space.name : `${space.name} (inativo)`}
          </option>
        ))}
      </SelectField>
      <div className={styles.row}>
        <TextField
          label="Data de início"
          inputMode="numeric"
          autoComplete="off"
          placeholder="dd/mm/aaaa"
          maxLength={10}
          error={formState.errors.startDate?.message}
          {...withMask(register('startDate'), maskDate)}
        />
        <TextField
          label="Hora de início"
          inputMode="numeric"
          autoComplete="off"
          placeholder="hh:mm"
          maxLength={5}
          error={formState.errors.startTime?.message}
          {...withMask(register('startTime'), maskTime)}
        />
      </div>
      <div className={styles.row}>
        <TextField
          label="Data de fim"
          inputMode="numeric"
          autoComplete="off"
          placeholder="dd/mm/aaaa"
          maxLength={10}
          error={formState.errors.endDate?.message}
          {...withMask(register('endDate'), maskDate)}
        />
        <TextField
          label="Hora de fim"
          inputMode="numeric"
          autoComplete="off"
          placeholder="hh:mm"
          maxLength={5}
          error={formState.errors.endTime?.message}
          {...withMask(register('endTime'), maskTime)}
        />
      </div>
      <TextAreaField
        label="Motivo"
        rows={3}
        maxLength={500}
        hint="Ex.: Manutenção ou reunião da diretoria. Até 500 caracteres."
        error={formState.errors.reason?.message}
        {...register('reason')}
      />
      <Button type="submit" disabled={isPending}>
        {isPending ? 'Salvando…' : 'Bloquear horário'}
      </Button>
    </FormCard>
  )
}
