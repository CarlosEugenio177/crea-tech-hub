import { Button } from '../../shared/ui/Button.tsx'
import { RecordCard } from '../../shared/ui/RecordCard.tsx'
import { formatBlockInterval, formatInstant } from './spaceBlockDates.ts'
import { useCancelSpaceBlock, type SpaceBlock } from './spaceBlocksApi.ts'

type SpaceBlockListItemProps = {
  block: SpaceBlock
  spaceName?: string
}

export function SpaceBlockListItem({
  block,
  spaceName,
}: SpaceBlockListItemProps) {
  const cancel = useCancelSpaceBlock(block.id)

  return (
    <RecordCard
      title={spaceName ?? block.spaceId}
      active={block.active}
      error={cancel.error}
      actions={
        block.active ? (
          <Button
            variant="secondary"
            disabled={cancel.isPending}
            onClick={cancel.cancel}
          >
            {cancel.isPending ? 'Cancelando…' : 'Cancelar bloqueio'}
          </Button>
        ) : undefined
      }
    >
      <p>{formatBlockInterval(block.startsAt, block.endsAt)}</p>
      <p>{block.reason}</p>
      {block.cancelledAt && (
        <p>Cancelado em {formatInstant(block.cancelledAt)}.</p>
      )}
    </RecordCard>
  )
}
