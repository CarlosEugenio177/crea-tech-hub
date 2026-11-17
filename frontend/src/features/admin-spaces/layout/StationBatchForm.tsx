import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Button } from '../../../shared/ui/Button.tsx'
import { ProblemNotice } from '../../../shared/ui/ProblemNotice.tsx'
import { TextField } from '../../../shared/ui/TextField.tsx'
import { useGenerateStations } from './roomLayoutApi.ts'
import styles from './RoomLayout.module.css'

const schema = z.object({
  count: z
    .number('Informe a quantidade.')
    .int('Use um número inteiro.')
    .min(1, 'Informe de 1 a 100 estações.')
    .max(100, 'Adicione até 100 estações por vez.'),
  columns: z
    .number('Informe as estações por fileira.')
    .int('Use um número inteiro.')
    .min(1, 'Informe de 1 a 100 estações por fileira.')
    .max(100, 'Informe até 100 estações por fileira.'),
})

type Props = { roomId: string; hasStations: boolean; disabled: boolean }

export function StationBatchForm({ roomId, hasStations, disabled }: Props) {
  const generate = useGenerateStations(roomId)
  const { register, handleSubmit, formState } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { count: hasStations ? 1 : 12, columns: 4 },
  })
  return (
    <form
      className={styles.panel}
      noValidate
      onSubmit={handleSubmit(generate.submit)}
    >
      <h2 className={styles.panelTitle}>
        {hasStations ? 'Adicionar estações' : 'Montagem rápida'}
      </h2>
      <TextField
        label="Quantidade de estações"
        type="number"
        min={1}
        max={100}
        step={1}
        disabled={disabled || generate.isPending}
        error={formState.errors.count?.message}
        {...register('count', { valueAsNumber: true })}
      />
      <TextField
        label="Estações por fileira"
        type="number"
        min={1}
        max={100}
        step={1}
        disabled={disabled || generate.isPending}
        error={formState.errors.columns?.message}
        {...register('columns', { valueAsNumber: true })}
      />
      <ProblemNotice error={generate.error} />
      <Button type="submit" disabled={disabled || generate.isPending}>
        {generate.isPending
          ? 'Gerando…'
          : hasStations
            ? 'Adicionar estações'
            : 'Gerar estações'}
      </Button>
      <p className={styles.hint}>
        As estações existentes mantêm seus números e posições.
      </p>
    </form>
  )
}
