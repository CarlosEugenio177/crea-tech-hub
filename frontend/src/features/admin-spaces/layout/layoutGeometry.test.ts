import { expect, test } from 'vitest'
import { snapPosition } from './layoutGeometry.ts'

test('extends a row with equal spacing even outside the regular grid', () => {
  const result = snapPosition(
    { x: 54.7, y: 39.8 },
    [
      { x: 21, y: 40 },
      { x: 38, y: 40 },
    ],
    { width: 600, height: 360 },
    { width: 10, length: 6 },
    1,
  )
  expect(result.point).toEqual({ x: 55, y: 40 })
  expect(result.guides).toContainEqual({ axis: 'x', value: 55, spacing: 17 })
})

test('does not pull a station to a distant line', () => {
  expect(
    snapPosition(
      { x: 46, y: 23 },
      [{ x: 12, y: 80 }],
      { width: 1000, height: 600 },
      { width: 10, length: 6 },
      1,
    ).point,
  ).toEqual({ x: 46, y: 23 })
})
