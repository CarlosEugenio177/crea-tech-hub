import { useParams } from 'react-router'
import { Button } from '../../../shared/ui/Button.tsx'
import { FormPage } from '../../../shared/ui/FormPage.tsx'
import { Notice } from '../../../shared/ui/Notice.tsx'
import { QueryStatus } from '../../../shared/ui/QueryStatus.tsx'
import { useSpace } from '../spacesApi.ts'
import { ADMIN_SPACES } from '../spaceRoutes.ts'
import { isStation } from './layoutTypes.ts'
import { RoomLayoutEditor } from './RoomLayoutEditor.tsx'
import { useRoomStations } from './roomLayoutApi.ts'

export function RoomLayoutPage() {
  const { id = '' } = useParams()
  const room = useSpace(id)
  const stations = useRoomStations(id, room.data?.type === 'ROOM')
  return (
    <FormPage
      eyebrow="Administração · Espaços"
      title="Montar sala"
      description={room.data?.name ?? 'Carregando sala…'}
      backTo={ADMIN_SPACES}
      backLabel="Voltar aos espaços"
    >
      {!room.data ? (
        <QueryStatus error={room.error} />
      ) : room.data.type !== 'ROOM' ? (
        <Notice tone="info">Este espaço não possui mapa de estações.</Notice>
      ) : (!stations.data || stations.hasNextPage) && stations.error ? (
        <>
          <QueryStatus error={stations.error} />
          <Button variant="secondary" onClick={() => void stations.refetch()}>
            Recarregar mapa
          </Button>
        </>
      ) : !stations.data || stations.hasNextPage ? (
        <QueryStatus error={undefined} />
      ) : (
        <RoomLayoutEditor
          key={id}
          room={room.data}
          stations={stations.data.filter(isStation)}
          fetching={stations.isFetching || room.isFetching}
          loadError={stations.error ?? room.error}
          onReload={() => {
            void room.refetch()
            void stations.refetch()
          }}
        />
      )}
    </FormPage>
  )
}
