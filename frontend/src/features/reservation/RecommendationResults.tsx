import { useEffect, useId, useRef } from 'react'
import { capacityLabel } from '../../shared/format/capacityLabel.ts'
import { Button } from '../../shared/ui/Button.tsx'
import { Notice } from '../../shared/ui/Notice.tsx'
import { QueryStatus } from '../../shared/ui/QueryStatus.tsx'
import { RecordCard } from '../../shared/ui/RecordCard.tsx'
import type { PublicSpace } from './publicSpacesApi.ts'
import { useSpaceRecommendations } from './recommendationsApi.ts'
import styles from './RecommendationResults.module.css'
import type { RecommendationCriteria } from './reservationDraft.ts'

type RecommendationResultsProps = {
  criteria: RecommendationCriteria
  onChoose: (space: PublicSpace) => void
}

export function RecommendationResults({
  criteria,
  onChoose,
}: RecommendationResultsProps) {
  const titleId = useId()
  const heading = useRef<HTMLHeadingElement>(null)
  const result = useSpaceRecommendations(criteria)

  useEffect(() => {
    heading.current?.focus()
  }, [criteria])

  return (
    <section className={styles.results} aria-labelledby={titleId}>
      <h2 id={titleId} tabIndex={-1} ref={heading}>
        Espaços disponíveis
      </h2>
      {result.isError ? (
        <>
          <QueryStatus error={result.error} />
          <Button variant="secondary" onClick={() => void result.refetch()}>
            Tentar novamente
          </Button>
        </>
      ) : !result.data ? (
        <QueryStatus error={undefined} />
      ) : result.data.status !== 'AVAILABLE' ? (
        <Notice tone="info">
          {result.data.message ?? 'Nenhum espaço atende esse horário.'}
        </Notice>
      ) : (
        <ul className={styles.list}>
          {result.data.spaces.map((space) => (
            <RecordCard
              key={space.id}
              title={space.name}
              actions={
                <Button
                  disabled={result.isFetching}
                  onClick={() => onChoose(space)}
                >
                  Escolher {space.name}
                </Button>
              }
            >
              <p>
                {space.bookingMode === 'PER_STATION'
                  ? '1 pessoa por estação'
                  : capacityLabel(space.capacity)}
              </p>
              {space.id === result.data.recommended?.id && (
                <strong>Recomendado</strong>
              )}
            </RecordCard>
          ))}
        </ul>
      )}
    </section>
  )
}
