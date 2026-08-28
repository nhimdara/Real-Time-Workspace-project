<template>
  <div class="my-3 select-none transition-all relative group/img-container w-full">
    <!-- 1. Display Mode: Image Rendered -->
    <div v-if="url && !showUploader" class="w-full flex flex-col space-y-1.5" :class="alignmentClass">
      <div
        class="relative group/img rounded-2xl overflow-hidden transition-all shadow-sm hover:shadow-md"
        :class="widthClass"
      >
        <img
          :src="url"
          :alt="caption || 'Document Image'"
          class="w-full h-auto rounded-2xl block transition-all mx-auto"
          :style="imageStyle"
          @dblclick="openLightbox"
        />

        <!-- Hover Actions Toolbar -->
        <div
          class="absolute top-3 right-3 opacity-0 group-hover/img:opacity-100 transition-opacity duration-200 flex items-center gap-1 bg-slate-900/85 backdrop-blur-md px-2 py-1 rounded-xl text-white shadow-xl z-20"
        >
          <!-- Crop / Aspect Ratio -->
          <button
            class="px-2 py-1 rounded-lg text-xs hover:bg-white/20 transition-colors flex items-center gap-1 text-slate-300 hover:text-white"
            title="Crop / Frame Image"
            @click="openCropModal"
          >
            <Crop class="w-3.5 h-3.5 text-brand-400" />
            <span class="hidden sm:inline">Crop</span>
          </button>

          <div class="w-px h-3 bg-white/20 mx-0.5"></div>

          <!-- Replace -->
          <button
            class="px-2 py-1 rounded-lg text-xs hover:bg-white/20 transition-colors flex items-center gap-1 text-slate-300 hover:text-white"
            title="Replace Image"
            @click="showUploader = true"
          >
            <RefreshCw class="w-3.5 h-3.5" />
            <span class="hidden sm:inline">Replace</span>
          </button>

          <div class="w-px h-3 bg-white/20 mx-0.5"></div>

          <!-- Alignment Buttons -->
          <div class="flex items-center gap-0.5">
            <button
              class="p-1 rounded-lg hover:bg-white/20 transition-colors"
              :class="alignment === 'left' ? 'bg-white/30 text-white' : 'text-slate-300'"
              title="Align Left"
              @click="setAlignment('left')"
            >
              <AlignLeft class="w-3.5 h-3.5" />
            </button>
            <button
              class="p-1 rounded-lg hover:bg-white/20 transition-colors"
              :class="(!alignment || alignment === 'center') ? 'bg-white/30 text-white' : 'text-slate-300'"
              title="Align Center"
              @click="setAlignment('center')"
            >
              <AlignCenter class="w-3.5 h-3.5" />
            </button>
            <button
              class="p-1 rounded-lg hover:bg-white/20 transition-colors"
              :class="alignment === 'right' ? 'bg-white/30 text-white' : 'text-slate-300'"
              title="Align Right"
              @click="setAlignment('right')"
            >
              <AlignRight class="w-3.5 h-3.5" />
            </button>
          </div>

          <div class="w-px h-3 bg-white/20 mx-0.5"></div>

          <!-- Width Scaling Presets -->
          <div class="flex items-center gap-0.5 text-[11px] font-mono font-medium">
            <button
              v-for="w in ['25%', '50%', '75%', '100%']"
              :key="w"
              class="px-1.5 py-0.5 rounded-md hover:bg-white/20 transition-colors"
              :class="(width === w || (!width && w === '100%')) ? 'bg-white/30 text-white font-bold' : 'text-slate-300'"
              :title="`Set width to ${w}`"
              @click="setWidth(w)"
            >
              {{ w }}
            </button>
          </div>

          <div class="w-px h-3 bg-white/20 mx-0.5"></div>

          <!-- Fullscreen Lightbox Preview -->
          <button
            class="p-1 rounded-lg hover:bg-white/20 text-slate-300 hover:text-white transition-colors"
            title="Preview Fullscreen"
            @click="openLightbox"
          >
            <Maximize2 class="w-3.5 h-3.5" />
          </button>

          <!-- Download Image -->
          <button
            class="p-1 rounded-lg hover:bg-white/20 text-slate-300 hover:text-white transition-colors"
            title="Download Image"
            @click="downloadImage"
          >
            <Download class="w-3.5 h-3.5" />
          </button>

          <!-- Remove Image -->
          <button
            class="p-1 rounded-lg hover:bg-red-500/50 text-red-400 hover:text-red-200 transition-colors"
            title="Delete Image"
            @click="emit('delete')"
          >
            <Trash2 class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      <!-- Caption Input -->
      <div class="px-2 flex items-center justify-center w-full">
        <input
          :value="caption"
          type="text"
          placeholder="Add a caption..."
          class="text-center text-xs text-slate-400 dark:text-slate-500 bg-transparent border-none outline-none focus:text-slate-700 dark:focus:text-slate-300 w-full max-w-xl transition-colors py-1"
          @input="emit('update:caption', ($event.target as HTMLInputElement).value)"
        />
      </div>
    </div>

    <!-- 2. Empty Upload Card / Replace Modal -->
    <div
      v-else
      class="p-6 rounded-2xl border-2 border-dashed border-slate-300 dark:border-[#353535] bg-slate-50/80 dark:bg-[#1e1e1e] text-center shadow-sm"
    >
      <!-- Navigation Tabs: Upload / Link / Unsplash -->
      <div class="flex items-center justify-center gap-1.5 mb-5 bg-slate-200/60 dark:bg-[#282828] p-1 rounded-2xl w-fit mx-auto">
        <button
          class="px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5"
          :class="activeTab === 'upload' ? 'bg-white dark:bg-[#333333] text-slate-900 dark:text-white shadow-sm' : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'"
          @click="activeTab = 'upload'"
        >
          <Upload class="w-3.5 h-3.5" /> Upload
        </button>
        <button
          class="px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5"
          :class="activeTab === 'link' ? 'bg-white dark:bg-[#333333] text-slate-900 dark:text-white shadow-sm' : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'"
          @click="activeTab = 'link'"
        >
          <Link2 class="w-3.5 h-3.5" /> Embed link
        </button>
        <button
          class="px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5"
          :class="activeTab === 'unsplash' ? 'bg-white dark:bg-[#333333] text-slate-900 dark:text-white shadow-sm' : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'"
          @click="activeTab = 'unsplash'"
        >
          <Sparkles class="w-3.5 h-3.5 text-amber-500" /> Unsplash
        </button>
      </div>

      <!-- Tab 1: Upload File (Drag & Drop or Dialog) -->
      <div v-if="activeTab === 'upload'" class="space-y-3 max-w-lg mx-auto">
        <div
          class="py-8 px-6 rounded-2xl border-2 border-dashed border-slate-300/80 dark:border-[#383838] hover:border-brand-500 dark:hover:border-brand-500 hover:bg-brand-50/30 dark:hover:bg-brand-950/20 transition-all cursor-pointer flex flex-col items-center gap-2.5"
          @click="triggerFileInput"
          @dragover.prevent
          @drop.prevent="handleDrop"
        >
          <div class="w-12 h-12 rounded-2xl bg-brand-50 dark:bg-brand-950/50 text-brand-600 dark:text-brand-400 flex items-center justify-center shadow-sm">
            <UploadCloud class="w-6 h-6 animate-bounce" />
          </div>
          <div class="space-y-0.5">
            <div class="text-xs font-semibold text-slate-800 dark:text-slate-200">
              <span class="text-brand-600 dark:text-brand-400 hover:underline">Choose an image</span> or drag and drop here
            </div>
            <p class="text-[11px] text-slate-400">Supports PNG, JPG, GIF, SVG, WebP (up to 20MB)</p>
          </div>
        </div>

        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleFileSelect"
        />

        <div v-if="isUploading" class="flex items-center justify-center gap-2 py-2 text-xs font-medium text-brand-600 dark:text-brand-400">
          <Loader2 class="w-4 h-4 animate-spin" /> Uploading image...
        </div>
      </div>

      <!-- Tab 2: Embed Link -->
      <div v-else-if="activeTab === 'link'" class="space-y-4 max-w-md mx-auto">
        <div class="flex items-center gap-2">
          <input
            v-model="linkInput"
            type="url"
            placeholder="Paste any image URL (e.g. https://...)"
            class="flex-1 px-3.5 py-2.5 rounded-xl text-xs bg-white dark:bg-[#282828] border border-slate-200 dark:border-[#383838] outline-none text-slate-900 dark:text-slate-100 focus:border-brand-500 focus:ring-1 focus:ring-brand-500/20"
            @keydown.enter="applyLink"
          />
          <button
            class="px-4 py-2.5 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 transition-colors shadow-sm shrink-0"
            :disabled="!linkInput.trim()"
            @click="applyLink"
          >
            Embed image
          </button>
        </div>
        <p class="text-[11px] text-slate-400">Works with any public image link from the web.</p>
      </div>

      <!-- Tab 3: Unsplash Search & Presets -->
      <div v-else-if="activeTab === 'unsplash'" class="space-y-4 max-w-xl mx-auto">
        <!-- Search bar -->
        <div class="relative">
          <Search class="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            v-model="unsplashQuery"
            type="text"
            placeholder="Search Unsplash photos (nature, minimal, tech, city...)"
            class="w-full pl-9 pr-3.5 py-2 rounded-xl text-xs bg-white dark:bg-[#282828] border border-slate-200 dark:border-[#383838] outline-none text-slate-900 dark:text-slate-100 focus:border-brand-500"
            @keydown.enter="searchUnsplash"
          />
        </div>

        <!-- Curated Category Tags -->
        <div class="flex items-center justify-center gap-1.5 flex-wrap">
          <button
            v-for="cat in ['Nature', 'Minimal', 'Architecture', 'Abstract', 'Workspace', 'Gradient']"
            :key="cat"
            class="px-2.5 py-1 rounded-lg text-[11px] font-medium bg-slate-200/70 dark:bg-[#282828] text-slate-600 dark:text-slate-300 hover:bg-brand-500 hover:text-white transition-colors"
            @click="quickSearchCategory(cat)"
          >
            {{ cat }}
          </button>
        </div>

        <!-- Presets Grid -->
        <div class="grid grid-cols-3 sm:grid-cols-4 gap-2.5 max-h-64 overflow-y-auto p-1 custom-scrollbar">
          <button
            v-for="(img, idx) in currentUnsplashList"
            :key="idx"
            class="group/item relative h-24 rounded-xl overflow-hidden border border-transparent hover:border-brand-500 hover:scale-[1.03] transition-all shadow-sm"
            @click="selectUnsplash(img)"
          >
            <img :src="img" class="w-full h-full object-cover" loading="lazy" />
            <div class="absolute inset-0 bg-black/30 opacity-0 group-hover/item:opacity-100 transition-opacity flex items-center justify-center text-white text-[10px] font-medium">
              Use Photo
            </div>
          </button>
        </div>
      </div>

      <!-- Cancel replace button -->
      <div v-if="url && showUploader" class="mt-4 pt-3 border-t border-slate-200 dark:border-[#2f2f2f]">
        <button
          class="text-xs font-medium text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 transition-colors"
          @click="showUploader = false"
        >
          Cancel
        </button>
      </div>
    </div>

    <!-- 3. Interactive Crop & Pixel-Drag Modal (Teleported to Body) -->
    <Teleport to="body">
      <div
        v-if="isCropModalOpen"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/90 backdrop-blur-md animate-fade-in p-4 select-none"
        @click.self="closeCropModal"
      >
        <div class="bg-slate-900 border border-slate-800 rounded-3xl p-6 max-w-3xl w-full flex flex-col gap-4 shadow-2xl animate-scale-in text-white">
          <!-- Header -->
          <div class="flex items-center justify-between border-b border-slate-800 pb-3">
            <div class="flex items-center gap-2">
              <Crop class="w-5 h-5 text-brand-400" />
              <div>
                <h3 class="text-sm font-bold">Crop & Frame Image</h3>
                <p class="text-[11px] text-slate-400">Drag image anywhere or use wheel to scroll up and down</p>
              </div>
            </div>
            <button
              class="p-1.5 rounded-xl hover:bg-white/10 text-slate-400 hover:text-white transition-colors"
              @click="closeCropModal"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <!-- Aspect Ratio Presets -->
          <div class="flex items-center justify-center gap-2 flex-wrap text-xs">
            <button
              v-for="preset in aspectPresets"
              :key="preset.id"
              class="px-3 py-1.5 rounded-xl font-medium transition-all"
              :class="cropRatio === preset.id ? 'bg-brand-600 text-white shadow-md' : 'bg-slate-800 text-slate-300 hover:bg-slate-700'"
              @click="setCropRatio(preset.id)"
            >
              {{ preset.label }}
            </button>
          </div>

          <!-- Interactive Drag / Scroll Preview Area -->
          <div class="relative w-full h-[360px] bg-slate-950 rounded-2xl overflow-hidden flex flex-col items-center justify-center border border-slate-800 py-3">
            <div
              ref="cropBoxRef"
              class="relative overflow-hidden rounded-xl border-2 border-dashed border-brand-500 transition-all flex items-center justify-center cursor-grab active:cursor-grabbing shadow-2xl bg-black"
              :style="cropBoxStyle"
              @mousedown="handleCropMouseDown"
              @wheel.prevent="handleCropWheel"
            >
              <img
                ref="cropImgRef"
                :src="url"
                alt="Crop preview"
                class="min-w-full min-h-full object-cover select-none pointer-events-none transition-transform"
                :style="{
                  transform: `translate(${cropPanX}px, ${cropPanY}px) scale(${cropScale}) rotate(${cropRotation}deg)`,
                }"
              />
              <div class="absolute inset-0 bg-transparent pointer-events-none border border-white/20"></div>
            </div>

            <div class="mt-2 text-[10px] text-slate-400 flex items-center gap-1.5">
              <Move class="w-3 h-3 text-brand-400" />
              <span>Click & drag to position or scroll mouse wheel</span>
            </div>
          </div>

          <!-- Zoom & Pan Controls -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs bg-slate-950/60 p-3 rounded-2xl border border-slate-800/80">
            <!-- Vertical Scroll (Y) -->
            <div class="flex items-center gap-2">
              <span class="text-slate-400 text-[11px] shrink-0 min-w-[70px]">Vertical (Y):</span>
              <input
                v-model.number="cropPanY"
                type="range"
                :min="-maxPanY"
                :max="maxPanY"
                step="1"
                class="w-full accent-brand-500 cursor-pointer"
              />
              <span class="font-mono text-slate-300 shrink-0 text-[11px] min-w-[36px] text-right">{{ cropPanY }}px</span>
            </div>

            <!-- Horizontal Scroll (X) -->
            <div class="flex items-center gap-2">
              <span class="text-slate-400 text-[11px] shrink-0 min-w-[70px]">Horizontal (X):</span>
              <input
                v-model.number="cropPanX"
                type="range"
                :min="-maxPanX"
                :max="maxPanX"
                step="1"
                class="w-full accent-brand-500 cursor-pointer"
              />
              <span class="font-mono text-slate-300 shrink-0 text-[11px] min-w-[36px] text-right">{{ cropPanX }}px</span>
            </div>

            <!-- Zoom Scale -->
            <div class="flex items-center gap-2">
              <span class="text-slate-400 text-[11px] shrink-0 min-w-[70px]">Zoom / Scale:</span>
              <input
                v-model.number="cropScale"
                type="range"
                min="1"
                max="2.5"
                step="0.05"
                class="w-full accent-brand-500 cursor-pointer"
              />
              <span class="font-mono text-slate-300 shrink-0 text-[11px] min-w-[36px] text-right">{{ Math.round(cropScale * 100) }}%</span>
            </div>

            <!-- Rotate & Reset -->
            <div class="flex items-center justify-end gap-2">
              <button
                class="px-2.5 py-1 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-[11px] transition-colors flex items-center gap-1"
                @click="rotateCrop"
              >
                <RotateCw class="w-3 h-3" /> Rotate
              </button>
              <button
                class="px-2.5 py-1 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-[11px] transition-colors"
                @click="resetCrop"
              >
                Reset
              </button>
            </div>
          </div>

          <!-- Modal Actions -->
          <div class="flex items-center justify-end gap-2.5 pt-2 border-t border-slate-800">
            <button
              class="px-4 py-2 rounded-xl text-xs font-medium text-slate-400 hover:text-white transition-colors"
              @click="closeCropModal"
            >
              Cancel
            </button>
            <button
              class="px-5 py-2 rounded-xl text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 transition-colors shadow-lg"
              @click="applyCrop"
            >
              Apply Crop & Framing
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 4. Fullscreen Lightbox / Zoom Modal (Teleported to Body) -->
    <Teleport to="body">
      <div
        v-if="isLightboxOpen"
        class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/95 backdrop-blur-lg animate-fade-in select-none"
        @click="closeLightbox"
      >
        <!-- Top Action Bar -->
        <div
          class="fixed top-5 right-5 flex items-center gap-2 bg-slate-900/90 border border-slate-700/60 backdrop-blur-md px-3 py-1.5 rounded-2xl shadow-2xl z-50 text-white"
          @click.stop
        >
          <button
            class="p-2 rounded-xl hover:bg-white/20 text-slate-300 hover:text-white transition-colors"
            title="Zoom In (+)"
            @click="zoomIn"
          >
            <ZoomIn class="w-4 h-4" />
          </button>
          <button
            class="p-2 rounded-xl hover:bg-white/20 text-slate-300 hover:text-white transition-colors"
            title="Zoom Out (-)"
            @click="zoomOut"
          >
            <ZoomOut class="w-4 h-4" />
          </button>
          <button
            v-if="lightboxZoom !== 1"
            class="px-2 py-1 rounded-xl hover:bg-white/20 text-xs text-brand-400 font-medium transition-colors"
            title="Reset Zoom"
            @click="lightboxZoom = 1"
          >
            Reset
          </button>
          <div class="w-px h-4 bg-white/20"></div>
          <button
            class="p-2 rounded-xl hover:bg-white/20 text-slate-300 hover:text-white transition-colors"
            title="Download Original"
            @click="downloadImage"
          >
            <Download class="w-4 h-4" />
          </button>
          <button
            class="p-2 rounded-xl hover:bg-red-500/80 text-slate-300 hover:text-white transition-colors"
            title="Close (Esc)"
            @click="closeLightbox"
          >
            <X class="w-4 h-4" />
          </button>
        </div>

        <!-- Lightbox Image Center Area -->
        <div
          class="w-full h-full flex flex-col items-center justify-center p-6 md:p-12 overflow-hidden"
          @click="closeLightbox"
        >
          <img
            :src="url"
            :alt="caption || 'Fullscreen View'"
            class="max-w-[92vw] max-h-[85vh] object-contain transition-transform duration-200 rounded-2xl shadow-2xl"
            :style="{ transform: `scale(${lightboxZoom})` }"
            @click.stop
          />
          <!-- Lightbox Caption -->
          <div
            v-if="caption"
            class="mt-4 px-4 py-1.5 rounded-xl bg-slate-900/80 text-center text-xs font-medium text-slate-300 max-w-lg truncate shadow-lg border border-slate-800"
            @click.stop
          >
            {{ caption }}
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  Upload,
  UploadCloud,
  Link2,
  Sparkles,
  RefreshCw,
  Loader2,
  Trash2,
  Maximize2,
  Download,
  AlignLeft,
  AlignCenter,
  AlignRight,
  Search,
  ZoomIn,
  ZoomOut,
  X,
  Crop,
  RotateCw,
  Move,
} from 'lucide-vue-next'
import api from '@/services/api'

const props = withDefaults(
  defineProps<{
    url?: string
    caption?: string
    width?: string
    alignment?: 'left' | 'center' | 'right'
    aspectRatio?: string
    cropPanX?: number
    cropPanY?: number
  }>(),
  {
    url: '',
    caption: '',
    width: '100%',
    alignment: 'center',
    aspectRatio: 'original',
    cropPanX: 0,
    cropPanY: 0,
  }
)

const emit = defineEmits<{
  (e: 'update:url', url: string): void
  (e: 'update:caption', caption: string): void
  (e: 'update:width', width: string): void
  (e: 'update:alignment', alignment: 'left' | 'center' | 'right'): void
  (e: 'update:aspectRatio', ratio: string): void
  (e: 'delete'): void
}>()

const activeTab = ref<'upload' | 'link' | 'unsplash'>('upload')
const showUploader = ref(false)
const isUploading = ref(false)
const linkInput = ref('')
const unsplashQuery = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)

// Lightbox state
const isLightboxOpen = ref(false)
const lightboxZoom = ref(1)

// Crop state
const isCropModalOpen = ref(false)
const cropRatio = ref<string>(props.aspectRatio || 'original')
const cropPanX = ref(0)
const cropPanY = ref(0)
const cropScale = ref(1)
const cropRotation = ref(0)
const cropImgRef = ref<HTMLImageElement | null>(null)
const cropBoxRef = ref<HTMLElement | null>(null)

const aspectPresets = [
  { id: 'original', label: 'Original (Full)' },
  { id: '16/9', label: '16:9 Landscape' },
  { id: '4/3', label: '4:3 Standard' },
  { id: '1/1', label: '1:1 Square' },
  { id: '3/4', label: '3:4 Portrait' },
  { id: '2/3', label: '2:3 Poster' },
]

const widthClass = computed(() => {
  switch (props.width) {
    case '25%': return 'w-1/4 max-w-sm'
    case '50%': return 'w-1/2 max-w-xl'
    case '75%': return 'w-3/4 max-w-3xl'
    case '100%': return 'w-full'
    default: return 'w-full'
  }
})

const alignmentClass = computed(() => {
  switch (props.alignment) {
    case 'left': return 'mr-auto items-start'
    case 'right': return 'ml-auto items-end'
    default: return 'mx-auto items-center'
  }
})

const imageStyle = computed(() => {
  if (props.aspectRatio && props.aspectRatio !== 'original') {
    return {
      aspectRatio: props.aspectRatio,
      objectFit: 'cover' as const,
      maxHeight: '650px',
    }
  }
  return {
    objectFit: 'contain' as const,
    maxHeight: 'none',
  }
})

const cropBoxStyle = computed(() => {
  switch (cropRatio.value) {
    case '16/9': return { width: '85%', height: '200px', aspectRatio: '16/9' }
    case '4/3': return { width: '70%', height: '240px', aspectRatio: '4/3' }
    case '1/1': return { width: '250px', height: '250px', aspectRatio: '1/1' }
    case '3/4': return { width: '220px', height: '293px', aspectRatio: '3/4' }
    case '2/3': return { width: '200px', height: '300px', aspectRatio: '2/3' }
    default: return { width: '85%', height: '300px' }
  }
})

function setAlignment(align: 'left' | 'center' | 'right') {
  emit('update:alignment', align)
}

function setWidth(w: string) {
  emit('update:width', w)
}

const maxPanX = computed(() => Math.round(60 * cropScale.value))
const maxPanY = computed(() => Math.round(90 * cropScale.value))

function clampCropPan(panX: number, panY: number): { x: number; y: number } {
  const limitX = maxPanX.value
  const limitY = maxPanY.value
  return {
    x: Math.max(-limitX, Math.min(limitX, panX)),
    y: Math.max(-limitY, Math.min(limitY, panY)),
  }
}

function setCropRatio(ratio: string) {
  cropRatio.value = ratio
  cropPanX.value = 0
  cropPanY.value = 0
}

function openCropModal() {
  cropRatio.value = props.aspectRatio || 'original'
  cropPanX.value = 0
  cropPanY.value = 0
  cropScale.value = 1
  cropRotation.value = 0
  isCropModalOpen.value = true
}

function closeCropModal() {
  isCropModalOpen.value = false
}

function rotateCrop() {
  cropRotation.value = (cropRotation.value + 90) % 360
}

function resetCrop() {
  cropRatio.value = 'original'
  cropPanX.value = 0
  cropPanY.value = 0
  cropScale.value = 1
  cropRotation.value = 0
}

// Drag & Wheel Panning in Crop Box with auto-clamping
let isDraggingCrop = false
let startDragX = 0
let startDragY = 0
let startCropPanX = 0
let startCropPanY = 0

function handleCropMouseDown(e: MouseEvent) {
  isDraggingCrop = true
  startDragX = e.clientX
  startDragY = e.clientY
  startCropPanX = cropPanX.value
  startCropPanY = cropPanY.value

  window.addEventListener('mousemove', handleCropMouseMove)
  window.addEventListener('mouseup', handleCropMouseUp)
}

function handleCropMouseMove(e: MouseEvent) {
  if (!isDraggingCrop) return
  const rawX = startCropPanX + (e.clientX - startDragX)
  const rawY = startCropPanY + (e.clientY - startDragY)
  const clamped = clampCropPan(rawX, rawY)
  cropPanX.value = clamped.x
  cropPanY.value = clamped.y
}

function handleCropMouseUp() {
  isDraggingCrop = false
  window.removeEventListener('mousemove', handleCropMouseMove)
  window.removeEventListener('mouseup', handleCropMouseUp)
}

function handleCropWheel(e: WheelEvent) {
  const rawY = cropPanY.value - Math.round(e.deltaY / 4)
  const clamped = clampCropPan(cropPanX.value, rawY)
  cropPanY.value = clamped.y
}

async function applyCrop() {
  if (!cropBoxRef.value) {
    closeCropModal()
    return
  }

  try {
    const box = cropBoxRef.value
    const boxRect = box.getBoundingClientRect()

    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      closeCropModal()
      return
    }

    const fullImg = new Image()
    fullImg.crossOrigin = 'anonymous'
    fullImg.src = props.url

    await new Promise((resolve, reject) => {
      fullImg.onload = resolve
      fullImg.onerror = reject
    })

    const targetW = Math.round(boxRect.width * 2)
    const targetH = Math.round(boxRect.height * 2)
    canvas.width = targetW
    canvas.height = targetH

    ctx.fillStyle = '#000000'
    ctx.fillRect(0, 0, targetW, targetH)

    ctx.save()
    ctx.translate(targetW / 2 + cropPanX.value * 2, targetH / 2 + cropPanY.value * 2)
    ctx.rotate((cropRotation.value * Math.PI) / 180)
    ctx.scale(cropScale.value, cropScale.value)

    const scaleToCover = Math.max(targetW / fullImg.width, targetH / fullImg.height)
    const drawW = fullImg.width * scaleToCover
    const drawH = fullImg.height * scaleToCover

    ctx.drawImage(fullImg, -drawW / 2, -drawH / 2, drawW, drawH)
    ctx.restore()

    const croppedDataUrl = canvas.toDataURL('image/png', 0.95)
    emit('update:url', croppedDataUrl)
    emit('update:aspectRatio', cropRatio.value)
  } catch (err) {
    emit('update:aspectRatio', cropRatio.value)
  } finally {
    closeCropModal()
  }
}

// Unsplash presets
const unsplashPresets = [
  'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?w=1400&auto=format&fit=crop&q=80',
]

const currentUnsplashList = ref<string[]>([...unsplashPresets])

function quickSearchCategory(cat: string) {
  unsplashQuery.value = cat
  searchUnsplash()
}

function searchUnsplash() {
  const q = unsplashQuery.value.trim().toLowerCase()
  if (!q) {
    currentUnsplashList.value = [...unsplashPresets]
    return
  }

  const seedKeywords = [
    `${q},modern`,
    `${q},minimal`,
    `${q},aesthetic`,
    `${q},creative`,
    `${q},dark`,
    `${q},vibrant`,
    `${q},abstract`,
    `${q},landscape`,
  ]

  currentUnsplashList.value = seedKeywords.map(
    (k, i) => `https://images.unsplash.com/photo-${1500000000000 + (Math.abs(hashString(k + i)) % 99999999)}?w=1400&auto=format&fit=crop&q=80&sig=${encodeURIComponent(k)}`
  ).slice(0, 8)
}

function hashString(str: string): number {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i)
    hash |= 0
  }
  return hash
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

async function handleFileSelect(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files && files[0]) {
    await uploadFile(files[0])
  }
}

async function handleDrop(e: DragEvent) {
  const files = e.dataTransfer?.files
  if (files && files[0]) {
    await uploadFile(files[0])
  }
}

async function uploadFile(file: File) {
  isUploading.value = true
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })

    if (res.data?.url) {
      emit('update:url', res.data.url)
      showUploader.value = false
    }
  } catch (err: any) {
    const reader = new FileReader()
    reader.onload = () => {
      emit('update:url', reader.result as string)
      showUploader.value = false
    }
    reader.readAsDataURL(file)
  } finally {
    isUploading.value = false
  }
}

function applyLink() {
  if (linkInput.value.trim()) {
    emit('update:url', linkInput.value.trim())
    linkInput.value = ''
    showUploader.value = false
  }
}

function selectUnsplash(url: string) {
  emit('update:url', url)
  showUploader.value = false
}

// Lightbox controls
function openLightbox() {
  isLightboxOpen.value = true
  lightboxZoom.value = 1
}

function closeLightbox() {
  isLightboxOpen.value = false
  lightboxZoom.value = 1
}

function zoomIn() {
  lightboxZoom.value = Math.min(3, lightboxZoom.value + 0.25)
}

function zoomOut() {
  lightboxZoom.value = Math.max(0.5, lightboxZoom.value - 0.25)
}

function downloadImage() {
  if (!props.url) return
  const link = document.createElement('a')
  link.href = props.url
  link.download = `image-${Date.now()}.png`
  link.target = '_blank'
  link.click()
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') {
    if (isCropModalOpen.value) closeCropModal()
    if (isLightboxOpen.value) closeLightbox()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #444;
  border-radius: 9999px;
}
</style>
