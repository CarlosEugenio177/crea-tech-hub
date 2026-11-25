import { useState } from 'react'
import { Button } from '../../shared/ui/Button.tsx'
import { RecordCard } from '../../shared/ui/RecordCard.tsx'
import { formatBlockInterval, formatInstant } from './spaceBlockDates.ts'
import {
  useCancelSpaceBlock,
  useCancelSpaceBlockSeries,
  type SpaceBlock,
} from './spaceBlocksApi.ts'

type SpaceBlockListItemProps = {
  block: SpaceBlock
  spaceName?: string
}

export function SpaceBlockListItem({
  block,
  spaceName,
}: SpaceBlockListItemProps) {
  const cancel = useCancelSpaceBlock(block.id)
  const series = useCancelSpaceBlockSeries()
  const [confirmSeries, setConfirmSeries] = useState(false)
  const isPending = cancel.isPending || series.isPending
  const seriesId = block.seriesId

  return (
    <RecordCard
      title={spaceName ?? block.spaceId}
      active={block.active}
      error={cancel.error ?? series.error}
      actions={
        block.active ? (
          <>
            {confirmSeries && seriesId ? (
              <>
                <Button
                  variant="secondary"
                  onClick={() => setConfirmSeries(false)}
                >
                  Voltar
                </Button>
                <Button
                  onClick={() => {
                    setConfirmSeries(false)
                    series.cancel(seriesId)
                  }}
                >
                  Confirmar cancelamento da série
                </Button>
              </>
            ) : (
              <>
                <Button
                  variant="secondary"
                  disabled={isPending}
                  onClick={cancel.cancel}
                >
                  {isPending ? 'Cancelando…' : 'Cancelar bloqueio'}
                </Button>
                {seriesId && (
                  <Button
                    variant="secondary"
                    disabled={isPending}
                    onClick={() => setConfirmSeries(true)}
                  >
                    Cancelar série
                  </Button>
                )}
              </>
            )}
          </>
        ) : undefined
      }
    >
      <p>{formatBlockInterval(block.startsAt, block.endsAt)}</p>
      <p>{block.reason}</p>
      {seriesId && <p>Recorrência semanal</p>}
      {confirmSeries && (
        <p>Os bloqueios restantes desta série serão cancelados.</p>
      )}
      {block.cancelledAt && (
        <p>Cancelado em {formatInstant(block.cancelledAt)}.</p>
      )}
    </RecordCard>
  )
}
