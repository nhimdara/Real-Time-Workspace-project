<template>
  <div class="my-3 overflow-x-auto rounded-2xl border border-slate-200 dark:border-[#2f2f2f] shadow-sm bg-white dark:bg-[#1f1f1f] select-none p-1">
    <!-- Top Table Header Options -->
    <div class="flex items-center justify-between px-3 py-1.5 border-b border-slate-100 dark:border-[#2a2a2a] text-[11px] text-slate-400">
      <div class="flex items-center gap-3">
        <label class="flex items-center gap-1.5 cursor-pointer hover:text-slate-700 dark:hover:text-slate-200">
          <input
            type="checkbox"
            :checked="hasHeaderRow"
            class="rounded text-brand-600 accent-brand-500 w-3.5 h-3.5"
            @change="toggleHeaderRow"
          />
          <span>Header row</span>
        </label>
        <label class="flex items-center gap-1.5 cursor-pointer hover:text-slate-700 dark:hover:text-slate-200">
          <input
            type="checkbox"
            :checked="hasHeaderCol"
            class="rounded text-brand-600 accent-brand-500 w-3.5 h-3.5"
            @change="toggleHeaderCol"
          />
          <span>Header column</span>
        </label>
      </div>

      <div class="flex items-center gap-2">
        <button
          class="px-2 py-0.5 rounded hover:bg-slate-100 dark:hover:bg-[#2a2a2a] hover:text-brand-500 transition-colors flex items-center gap-1"
          @click="addColumn"
        >
          <Plus class="w-3 h-3" /> Add Column
        </button>
        <button
          class="px-2 py-0.5 rounded hover:bg-slate-100 dark:hover:bg-[#2a2a2a] hover:text-brand-500 transition-colors flex items-center gap-1"
          @click="addRow"
        >
          <Plus class="w-3 h-3" /> Add Row
        </button>
      </div>
    </div>

    <!-- Table Matrix Grid -->
    <div class="overflow-x-auto">
      <table class="w-full border-collapse text-xs font-sans">
        <tbody>
          <tr v-for="(row, rIdx) in grid" :key="rIdx" class="group/row border-b border-slate-100 dark:border-[#2a2a2a] last:border-b-0">
            <td
              v-for="(cell, cIdx) in row"
              :key="cIdx"
              :class="[
                'border-r border-slate-100 dark:border-[#2a2a2a] last:border-r-0 p-0 relative transition-colors',
                rIdx === 0 && hasHeaderRow ? 'bg-slate-50 dark:bg-[#252525] font-semibold text-slate-900 dark:text-white' : '',
                cIdx === 0 && hasHeaderCol ? 'bg-slate-50 dark:bg-[#252525] font-semibold text-slate-900 dark:text-white' : 'text-slate-800 dark:text-slate-200'
              ]"
            >
              <input
                :value="cell"
                type="text"
                placeholder="Empty"
                class="w-full min-w-[120px] px-3 py-2 bg-transparent outline-none border-none placeholder-slate-300 dark:placeholder-slate-600 text-xs"
                @input="updateCell(rIdx, cIdx, ($event.target as HTMLInputElement).value)"
              />
            </td>

            <!-- Row Delete on Hover -->
            <td class="w-8 p-0 text-center opacity-0 group-hover/row:opacity-100 transition-opacity">
              <button
                v-if="grid.length > 1"
                class="p-1 rounded text-slate-400 hover:text-red-500 hover:bg-slate-100 dark:hover:bg-[#2a2a2a]"
                title="Delete Row"
                @click="deleteRow(rIdx)"
              >
                <Trash2 class="w-3 h-3" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Trash2 } from 'lucide-vue-next'

const props = defineProps<{
  tableData?: string[][]
  hasHeaderRow?: boolean
  hasHeaderCol?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:tableData', data: string[][]): void
  (e: 'update:hasHeaderRow', val: boolean): void
  (e: 'update:hasHeaderCol', val: boolean): void
}>()

const defaultGrid: string[][] = [
  ['Feature', 'Status', 'Assignee'],
  ['Auth JWT', 'Done', 'Sarah'],
  ['Real-time Sync', 'In Progress', 'Alex'],
]

const grid = computed(() => {
  return props.tableData && props.tableData.length > 0 ? props.tableData : defaultGrid
})

function updateCell(rIdx: number, cIdx: number, value: string) {
  const newGrid = grid.value.map((r, rI) =>
    rI === rIdx ? r.map((c, cI) => (cI === cIdx ? value : c)) : [...r]
  )
  emit('update:tableData', newGrid)
}

function addRow() {
  const colCount = grid.value[0]?.length || 3
  const newRow = new Array(colCount).fill('')
  const newGrid = [...grid.value.map((r) => [...r]), newRow]
  emit('update:tableData', newGrid)
}

function addColumn() {
  const newGrid = grid.value.map((r) => [...r, ''])
  emit('update:tableData', newGrid)
}

function deleteRow(rIdx: number) {
  if (grid.value.length <= 1) return
  const newGrid = grid.value.filter((_, idx) => idx !== rIdx)
  emit('update:tableData', newGrid)
}

function toggleHeaderRow() {
  emit('update:hasHeaderRow', !props.hasHeaderRow)
}

function toggleHeaderCol() {
  emit('update:hasHeaderCol', !props.hasHeaderCol)
}
</script>
