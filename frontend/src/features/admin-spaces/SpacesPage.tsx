import { useState } from 'react'
import { ButtonLink } from '../../shared/ui/ButtonLink.tsx'
import { ListPage } from '../../shared/ui/ListPage.tsx'
import { PagedList } from '../../shared/ui/PagedList.tsx'
import { SelectField } from '../../shared/ui/SelectField.tsx'
import { SpaceListItem } from './SpaceListItem.tsx'
import styles from './SpacesPage.module.css'
import { ADMIN_SPACE_NEW } from './spaceRoutes.ts'
import { useSpaces } from './spacesApi.ts'

export function SpacesPage() {
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const spaces = useSpaces(page, status === '' ? undefined : status === 'true')

  return (
    <ListPage
      title="Espaços"
      actions={<ButtonLink to={ADMIN_SPACE_NEW}>Novo espaço</ButtonLink>}
    >
      <div className={styles.filter}>
        <SelectField
          label="Situação"
          value={status}
          onChange={(event) => {
            setStatus(event.target.value)
            setPage(0)
          }}
        >
          <option value="">Todos</option>
          <option value="true">Ativos</option>
          <option value="false">Inativos</option>
        </SelectField>
      </div>
      <PagedList
        data={spaces.data}
        error={spaces.error}
        page={page}
        onPageChange={setPage}
        emptyText={
          status === '' && page === 0
            ? 'Nenhum espaço cadastrado.'
            : 'Nenhum espaço encontrado.'
        }
      >
        {(space) => <SpaceListItem key={space.id} space={space} />}
      </PagedList>
    </ListPage>
  )
}
