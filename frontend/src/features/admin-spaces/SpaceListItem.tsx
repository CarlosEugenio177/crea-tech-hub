import { Button } from '../../shared/ui/Button.tsx'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { RecordCard } from '../../shared/ui/RecordCard.tsx'
import { capacityLabel, SPACE_TYPE_LABELS } from './spaceLabels.ts'
import { spaceEditPath } from './spaceRoutes.ts'
import { useChangeSpaceStatus, type Space } from './spacesApi.ts'

type SpaceListItemProps = { space: Space }

export function SpaceListItem({ space }: SpaceListItemProps) {
  const status = useChangeSpaceStatus(space.id)

  return (
    <RecordCard
      title={space.name}
      active={space.active}
      error={status.error}
      actions={
        <>
          <ButtonLink variant="secondary" to={spaceEditPath(space.id)}>
            Editar
          </ButtonLink>
          <Button
            variant="secondary"
            disabled={status.isPending}
            onClick={() => status.change(!space.active)}
          >
            {space.active ? 'Desativar' : 'Ativar'}
          </Button>
        </>
      }
    >
      <p>
        {SPACE_TYPE_LABELS[space.type]} · {capacityLabel(space.capacity)}
      </p>
      {space.description && <p>{space.description}</p>}
    </RecordCard>
  )
}
