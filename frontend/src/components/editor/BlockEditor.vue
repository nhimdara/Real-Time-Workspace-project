<template>
  <div class="relative">
    <!-- Floating Text Selection Bubble Toolbar -->
    <FloatingToolbar />

    <!-- Notion Page Cover Image Banner -->
    <div v-if="pageCoverUrl" class="relative w-full h-48 md:h-64 group/cover overflow-hidden bg-slate-900">
      <img
        :src="pageCoverUrl"
        class="w-full h-full object-cover"
        alt="Page Cover"
      />
      <div class="absolute inset-0 bg-black/20 opacity-0 group-hover/cover:opacity-100 transition-opacity flex items-end justify-end p-4 gap-2">
        <button
          class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-900/80 text-white backdrop-blur-md hover:bg-slate-900 transition-colors flex items-center gap-1.5"
          @click="showCoverModal = true"
        >
          <ImageIcon class="w-3.5 h-3.5" /> Change cover
        </button>
        <button
          class="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-900/80 text-red-400 backdrop-blur-md hover:bg-slate-900 transition-colors flex items-center gap-1.5"
          @click="removeCover"
        >
          <Trash2 class="w-3.5 h-3.5" /> Remove
        </button>
      </div>
    </div>

    <!-- Main Document Canvas with Dynamic Typography Styles -->
    <div
      ref="editorContainerRef"
      :class="[
        'mx-auto px-8 md:px-16 py-8 relative min-h-[calc(100vh-140px)] transition-all duration-200',
        isFullWidth ? 'max-w-7xl' : 'max-w-4xl',
        fontStyle === 'serif' ? 'font-serif' : fontStyle === 'mono' ? 'font-mono' : 'font-sans',
        isSmallText ? 'text-xs' : 'text-sm'
      ]"
      @mousemove="handleMouseMove"
    >
      <!-- Page Header Controls -->
      <div class="mb-8 group">
        <!-- Top Toolbar: Cover toggle, AI Button, Comments, Font, Word Count, Export, Width Toggle -->
        <div class="flex items-center justify-between gap-3 mb-4">
          <div class="flex items-center gap-3">
            <button
              class="text-4xl hover:scale-110 transition-transform p-2 rounded-2xl liquid-glass hover:bg-white/80 dark:hover:bg-white/20 border border-white/40 dark:border-white/10 shadow-sm"
              title="Change Page Icon"
              @click="changeIcon"
            >
              {{ currentPage?.icon || '📄' }}
            </button>

            <button
              v-if="!pageCoverUrl"
              class="opacity-0 group-hover:opacity-100 text-xs font-semibold text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-slate-100 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/15 px-3 py-1.5 rounded-xl border border-white/40 dark:border-white/10 transition-all flex items-center gap-1.5 shadow-2xs backdrop-blur-md"
              @click="showCoverModal = true"
            >
              <ImageIcon class="w-3.5 h-3.5" /> Add cover
            </button>
          </div>

          <div class="flex items-center gap-2 overflow-x-auto max-w-full py-0.5 scrollbar-none">
            <!-- Quick Insert Image Button -->
            <button
              class="px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap shrink-0 text-slate-600 dark:text-slate-300 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/15 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all flex items-center gap-1.5 shadow-2xs"
              title="Insert Image"
              @click="triggerQuickImageUpload"
            >
              <ImageIcon class="w-3.5 h-3.5 text-brand-500 shrink-0" />
              <span>Image</span>
            </button>
            <input
              ref="quickImageFileInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleQuickImageUpload"
            />

            <!-- Notion AI Trigger Button -->
            <button
              class="px-3 py-1.5 rounded-xl text-xs font-bold whitespace-nowrap shrink-0 text-purple-600 dark:text-purple-300 bg-purple-500/10 hover:bg-purple-500/20 border border-purple-500/30 backdrop-blur-md transition-all flex items-center gap-1.5 shadow-sm"
              title="Open Notion AI Assistant"
              @click="showAIModal = true"
            >
              <Sparkles class="w-3.5 h-3.5 text-purple-500 animate-pulse shrink-0" />
              <span>Notion AI</span>
            </button>

            <!-- Page Comments Trigger -->
            <button
              class="px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap shrink-0 text-slate-600 dark:text-slate-300 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/15 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all flex items-center gap-1.5 shadow-2xs"
              title="Page Discussion"
              @click="showComments = !showComments"
            >
              <MessageSquare class="w-3.5 h-3.5 shrink-0" />
              <span>Comments</span>
            </button>

            <!-- Typography Font Selector -->
            <div class="relative shrink-0">
              <button
                class="px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap text-slate-600 dark:text-slate-300 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/15 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all flex items-center gap-1 shadow-2xs"
                @click="showFontMenu = !showFontMenu"
              >
                <span>{{ fontStyle === 'serif' ? 'Serif' : fontStyle === 'mono' ? 'Mono' : 'Default' }}</span>
                <ChevronDown class="w-3 h-3 shrink-0" />
              </button>

              <!-- Font Dropdown -->
              <div
                v-if="showFontMenu"
                class="absolute right-0 top-full mt-1.5 w-40 rounded-2xl liquid-glass-modal p-1.5 z-50 space-y-0.5 text-xs select-none shadow-2xl"
              >
                <button
                  class="w-full px-3 py-2 rounded-xl text-left font-sans hover:bg-white/60 dark:hover:bg-white/10 flex items-center justify-between font-semibold whitespace-nowrap"
                  @click="fontStyle = 'sans'; showFontMenu = false"
                >
                  <span>Default (Sans)</span>
                  <Check v-if="fontStyle === 'sans'" class="w-3.5 h-3.5 text-brand-500 shrink-0" />
                </button>
                <button
                  class="w-full px-3 py-2 rounded-xl text-left font-serif hover:bg-white/60 dark:hover:bg-white/10 flex items-center justify-between font-semibold whitespace-nowrap"
                  @click="fontStyle = 'serif'; showFontMenu = false"
                >
                  <span>Serif</span>
                  <Check v-if="fontStyle === 'serif'" class="w-3.5 h-3.5 text-brand-500 shrink-0" />
                </button>
                <button
                  class="w-full px-3 py-2 rounded-xl text-left font-mono hover:bg-white/60 dark:hover:bg-white/10 flex items-center justify-between font-semibold whitespace-nowrap"
                  @click="fontStyle = 'mono'; showFontMenu = false"
                >
                  <span>Mono</span>
                  <Check v-if="fontStyle === 'mono'" class="w-3.5 h-3.5 text-brand-500 shrink-0" />
                </button>
                <div class="border-t border-white/30 dark:border-white/10 my-1"></div>
                <button
                  class="w-full px-3 py-2 rounded-xl text-left hover:bg-white/60 dark:hover:bg-white/10 flex items-center justify-between text-[11px] font-semibold whitespace-nowrap"
                  @click="isSmallText = !isSmallText"
                >
                  <span>Small text</span>
                  <Check v-if="isSmallText" class="w-3.5 h-3.5 text-brand-500 shrink-0" />
                </button>
              </div>
            </div>

            <!-- Export to Markdown -->
            <button
              class="px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap shrink-0 text-slate-600 dark:text-slate-300 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/15 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all flex items-center gap-1.5 shadow-2xs"
              title="Export as Markdown (.md)"
              @click="exportMarkdown"
            >
              <Download class="w-3.5 h-3.5 shrink-0" />
              <span>Export</span>
            </button>

            <!-- Full Width Toggle -->
            <button
              class="px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap shrink-0 text-slate-600 dark:text-slate-300 bg-white/40 dark:bg-white/5 hover:bg-white/80 dark:hover:bg-white/15 border border-white/40 dark:border-white/10 backdrop-blur-md transition-all flex items-center gap-1.5 shadow-2xs"
              :title="isFullWidth ? 'Switch to Standard Width' : 'Switch to Full Width'"
              @click="isFullWidth = !isFullWidth"
            >
              <Maximize2 v-if="!isFullWidth" class="w-3.5 h-3.5 shrink-0" />
              <Minimize2 v-else class="w-3.5 h-3.5 shrink-0" />
              <span>{{ isFullWidth ? 'Standard' : 'Full width' }}</span>
            </button>
          </div>
        </div>

        <input
          v-model="pageTitle"
          type="text"
          placeholder="Untitled"
          class="w-full text-4xl font-extrabold bg-transparent border-none outline-none text-slate-900 dark:text-slate-100 placeholder-slate-400 dark:placeholder-slate-600 tracking-tight"
          @blur="savePageTitle"
          @keydown.enter.prevent="focusFirstBlock"
        />
      </div>

      <!-- Draggable Blocks List -->
      <draggable
        v-model="draggableBlocks"
        item-key="id"
        handle=".drag-handle"
        ghost-class="ghost-card"
        animation="200"
        class="space-y-1 pb-32"
        @end="onDragEnd"
      >
        <template #item="{ element: block, index }">
          <div
            :key="block.id"
            class="group relative rounded-lg px-2 py-0.5 -mx-2 transition-colors"
            :class="block.type === 'image' ? 'hover:bg-transparent' : 'hover:bg-slate-100/60 dark:hover:bg-slate-800/40'"
          >
            <!-- Notion-Style Left Gutter Drag Handle & Actions -->
            <div
              class="absolute -left-14 top-1 opacity-0 group-hover:opacity-100 flex items-center gap-0.5 transition-opacity select-none z-10"
            >
              <button
                class="p-1 text-slate-400 hover:text-brand-600 dark:hover:text-brand-400 rounded hover:bg-slate-200 dark:hover:bg-slate-700"
                title="Add block below"
                @click="addNewBlockAfter(index)"
              >
                <Plus class="w-3.5 h-3.5" />
              </button>
              <button
                class="drag-handle p-1 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 cursor-grab active:cursor-grabbing rounded hover:bg-slate-200 dark:hover:bg-slate-700"
                title="Click for options, drag to reorder"
                @click.stop="openBlockMenu(block, $event)"
              >
                <GripVertical class="w-3.5 h-3.5" />
              </button>
              <button
                class="p-1 text-slate-400 hover:text-red-500 rounded hover:bg-slate-200 dark:hover:bg-slate-700"
                title="Delete block"
                @click="removeBlock(block.id)"
              >
                <Trash2 class="w-3.5 h-3.5" />
              </button>
            </div>

            <!-- Dynamic Block Content Renderer (Flush Alignment) -->
            <div class="w-full min-w-0">
              <!-- HEADING 1 -->
              <EditableBlock
                v-if="block.type === 'heading_1'"
                :id="`block-${block.id}`"
                type="heading_1"
                :model-value="block.content.text || ''"
                placeholder="Heading 1"
                @update:model-value="onTextChange(block, $event)"
                @input="onInput(block, $event)"
                @keydown="onKeydown(block, index, $event)"
              />

              <!-- HEADING 2 -->
              <EditableBlock
                v-else-if="block.type === 'heading_2'"
                :id="`block-${block.id}`"
                type="heading_2"
                :model-value="block.content.text || ''"
                placeholder="Heading 2"
                @update:model-value="onTextChange(block, $event)"
                @input="onInput(block, $event)"
                @keydown="onKeydown(block, index, $event)"
              />

              <!-- HEADING 3 -->
              <EditableBlock
                v-else-if="block.type === 'heading_3'"
                :id="`block-${block.id}`"
                type="heading_3"
                :model-value="block.content.text || ''"
                placeholder="Heading 3"
                @update:model-value="onTextChange(block, $event)"
                @input="onInput(block, $event)"
                @keydown="onKeydown(block, index, $event)"
              />

              <!-- TODO ITEM -->
              <div v-else-if="block.type === 'todo'" class="flex items-start gap-2.5 my-0.5">
                <input
                  type="checkbox"
                  :checked="block.content.checked || false"
                  class="mt-1 w-4 h-4 rounded border-slate-300 dark:border-slate-700 text-brand-600 focus:ring-brand-500 cursor-pointer accent-brand-500"
                  @change="toggleTodo(block)"
                />
                <EditableBlock
                  :id="`block-${block.id}`"
                  type="todo"
                  :model-value="block.content.text || ''"
                  :custom-class="block.content.checked ? 'line-through text-slate-400 dark:text-slate-500 flex-1' : 'text-slate-800 dark:text-slate-200 flex-1'"
                  placeholder="To-do item"
                  @update:model-value="onTextChange(block, $event)"
                  @input="onInput(block, $event)"
                  @keydown="onKeydown(block, index, $event)"
                />
              </div>

              <!-- BULLET LIST -->
              <div v-else-if="block.type === 'bullet_list'" class="flex items-start gap-2.5 my-0.5">
                <span class="text-slate-400 select-none text-base leading-5">•</span>
                <EditableBlock
                  :id="`block-${block.id}`"
                  type="bullet_list"
                  :model-value="block.content.text || ''"
                  custom-class="flex-1 text-slate-800 dark:text-slate-200"
                  placeholder="List item"
                  @update:model-value="onTextChange(block, $event)"
                  @input="onInput(block, $event)"
                  @keydown="onKeydown(block, index, $event)"
                />
              </div>

              <!-- NUMBERED LIST -->
              <div v-else-if="block.type === 'numbered_list'" class="flex items-start gap-2.5 my-0.5">
                <span class="text-slate-400 select-none text-xs font-semibold pt-1 min-w-[16px]">{{ index + 1 }}.</span>
                <EditableBlock
                  :id="`block-${block.id}`"
                  type="numbered_list"
                  :model-value="block.content.text || ''"
                  custom-class="flex-1 text-slate-800 dark:text-slate-200"
                  placeholder="Numbered item"
                  @update:model-value="onTextChange(block, $event)"
                  @input="onInput(block, $event)"
                  @keydown="onKeydown(block, index, $event)"
                />
              </div>

              <!-- TOGGLE LIST -->
              <div v-else-if="block.type === 'toggle'" class="my-1">
                <div class="flex items-start gap-2">
                  <button
                    class="p-0.5 rounded text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 transition-transform mt-0.5"
                    :class="block.content.isOpen ? 'rotate-90' : ''"
                    @click="toggleExpand(block)"
                  >
                    <ChevronRight class="w-4 h-4" />
                  </button>
                  <EditableBlock
                    :id="`block-${block.id}`"
                    type="toggle"
                    :model-value="block.content.text || ''"
                    custom-class="flex-1 font-medium text-slate-800 dark:text-slate-200"
                    placeholder="Toggle heading"
                    @update:model-value="onTextChange(block, $event)"
                    @input="onInput(block, $event)"
                    @keydown="onKeydown(block, index, $event)"
                  />
                </div>
                <div v-if="block.content.isOpen" class="pl-6 pt-1 border-l border-slate-200 dark:border-slate-800 ml-2.5 my-1">
                  <p class="text-xs text-slate-400 italic">Toggle details and sub-blocks</p>
                </div>
              </div>

              <!-- QUOTE BLOCK -->
              <EditableBlock
                v-else-if="block.type === 'quote'"
                :id="`block-${block.id}`"
                type="quote"
                :model-value="block.content.text || ''"
                placeholder="Empty quote..."
                @update:model-value="onTextChange(block, $event)"
                @input="onInput(block, $event)"
                @keydown="onKeydown(block, index, $event)"
              />

              <!-- CALLOUT BANNER (Liquid Glass) -->
              <div
                v-else-if="block.type === 'callout'"
                class="flex items-start gap-3.5 p-4 my-3 rounded-2xl bg-gradient-to-r from-brand-500/15 to-emerald-400/10 backdrop-blur-md border border-brand-500/30 text-sm text-slate-900 dark:text-slate-100 shadow-lg shadow-brand-500/10"
              >
                <span class="text-xl select-none p-1 rounded-xl bg-white/40 dark:bg-white/10 backdrop-blur-sm border border-white/40 dark:border-white/10 shadow-2xs">{{ block.content.icon || '💡' }}</span>
                <EditableBlock
                  :id="`block-${block.id}`"
                  type="callout"
                  :model-value="block.content.text || ''"
                  custom-class="flex-1 font-medium"
                  placeholder="Type a callout message..."
                  @update:model-value="onTextChange(block, $event)"
                  @input="onInput(block, $event)"
                  @keydown="onKeydown(block, index, $event)"
                />
              </div>

              <!-- TABLE BLOCK (Notion Matrix Grid) -->
              <TableBlock
                v-else-if="block.type === 'table'"
                :table-data="block.content.tableData"
                :has-header-row="block.content.hasHeaderRow ?? true"
                :has-header-col="block.content.hasHeaderCol ?? false"
                @update:table-data="onTableDataChange(block, $event)"
                @update:has-header-row="onTableHeaderRowChange(block, $event)"
                @update:has-header-col="onTableHeaderColChange(block, $event)"
              />

              <!-- TABLE OF CONTENTS BLOCK -->
              <TableOfContents
                v-else-if="block.type === 'toc'"
              />

              <!-- IMAGE BLOCK (Upload / Link / Unsplash / Crop) -->
              <ImageBlock
                v-else-if="block.type === 'image'"
                :url="block.content.url"
                :caption="block.content.caption"
                :width="block.content.width"
                :alignment="block.content.alignment"
                :aspect-ratio="block.content.aspectRatio"
                :object-position="block.content.objectPosition"
                @update:url="onImageUrlChange(block, $event)"
                @update:caption="onImageCaptionChange(block, $event)"
                @update:width="onImageWidthChange(block, $event)"
                @update:alignment="onImageAlignmentChange(block, $event)"
                @update:aspect-ratio="onImageAspectRatioChange(block, $event)"
                @update:object-position="onImageObjectPositionChange(block, $event)"
                @delete="removeBlock(block.id)"
              />

              <!-- CODE BLOCK (Multi-language + Copy) -->
              <CodeBlock
                v-else-if="block.type === 'code'"
                :id="`block-${block.id}`"
                :model-value-text="block.content.text || ''"
                :model-value-language="block.content.language || 'typescript'"
                @update:model-value-text="onTextChange(block, $event)"
                @update:model-value-language="onLanguageChange(block, $event)"
                @exit-below="addNewBlockAfter(index)"
                @input="onInput(block, $event)"
                @keydown="onKeydown(block, index, $event)"
              />

              <!-- DIVIDER -->
              <div v-else-if="block.type === 'divider'" class="py-2.5 my-1">
                <hr class="border-t border-slate-200 dark:border-slate-800" />
              </div>

              <!-- DEFAULT PARAGRAPH -->
              <EditableBlock
                v-else
                :id="`block-${block.id}`"
                type="paragraph"
                :model-value="block.content.text || ''"
                placeholder="Type '/' for commands or start writing..."
                @update:model-value="onTextChange(block, $event)"
                @input="onInput(block, $event)"
                @keydown="onKeydown(block, index, $event)"
              />
            </div>
          </div>
        </template>
      </draggable>

      <!-- Clickable Empty Canvas Area (Click anywhere to start new line) -->
      <div
        class="canvas-empty-area h-64 cursor-text -mx-8 px-8"
        title="Click to write..."
        @click="handleCanvasClick"
      ></div>

      <!-- Empty State Add First Block Button -->
      <div v-if="draggableBlocks.length === 0" class="py-12 text-center">
        <button
          class="inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-medium text-brand-600 dark:text-brand-400 bg-brand-50 dark:bg-brand-950/50 hover:bg-brand-100 transition-colors"
          @click="addNewBlockAfter(-1)"
        >
          <Plus class="w-4 h-4" /> Click or press Enter to start writing
        </button>
      </div>

      <!-- Slash Commands Floating Menu -->
      <SlashMenu
        :is-open="slashMenuOpen"
        :position="slashMenuPosition"
        :query="slashQuery"
        @select="handleSlashSelect"
        @close="closeSlashMenu"
      />

      <!-- Block Context 6-Dot Menu -->
      <BlockActionsMenu
        :is-open="blockMenuOpen"
        :position="blockMenuPosition"
        @delete="deleteActiveMenuBlock"
        @duplicate="duplicateActiveMenuBlock"
        @turn-into="turnIntoType"
        @close="blockMenuOpen = false"
      />

      <!-- Notion AI Assistant Modal -->
      <NotionAIAssistant
        :is-open="showAIModal"
        @close="showAIModal = false"
      />

      <!-- Comments Side Panel -->
      <CommentsPanel
        :is-open="showComments"
        @close="showComments = false"
      />
    </div>

    <!-- Cover Selector Modal (Liquid Glass) -->
    <div
      v-if="showCoverModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 backdrop-blur-md animate-fade-in"
      @click.self="showCoverModal = false"
    >
      <div class="w-full max-w-md p-6 rounded-3xl liquid-glass-modal animate-scale-in">
        <h3 class="text-base font-bold text-slate-900 dark:text-slate-100 mb-4">Choose Page Cover</h3>
        
        <!-- Preset Gradients & Photos -->
        <div class="grid grid-cols-3 gap-2.5 mb-4">
          <button
            v-for="(img, idx) in coverPresets"
            :key="idx"
            class="h-20 rounded-2xl overflow-hidden border-2 transition-all hover:scale-105 shadow-sm"
            :class="pageCoverUrl === img ? 'border-brand-500 ring-4 ring-brand-500/30' : 'border-white/40 dark:border-white/10'"
            @click="setCover(img)"
          >
            <img :src="img" class="w-full h-full object-cover" />
          </button>
        </div>

        <label class="block text-xs font-medium text-slate-600 dark:text-slate-400 mb-1.5">Paste Image URL or Upload</label>
        <div class="flex items-center gap-2 mb-3">
          <input
            v-model="customCoverInput"
            type="url"
            placeholder="https://images.unsplash.com/..."
            class="flex-1 px-3.5 py-2.5 text-xs rounded-xl liquid-glass-input text-slate-900 dark:text-slate-100 outline-none"
            @keydown.enter="setCover(customCoverInput)"
          />
          <button
            class="px-4 py-2.5 rounded-xl text-xs font-bold text-white liquid-glass-btn"
            @click="setCover(customCoverInput)"
          >
            Apply
          </button>
        </div>

        <!-- Upload File Button for Cover -->
        <label class="w-full py-2.5 rounded-xl border border-dashed border-slate-300 dark:border-slate-700 hover:border-brand-500 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:text-brand-500 bg-white/20 dark:bg-white/5 hover:bg-white/50 dark:hover:bg-white/10 flex items-center justify-center gap-2 cursor-pointer transition-all shadow-2xs">
          <Upload class="w-3.5 h-3.5" /> Upload cover from computer
          <input type="file" accept="image/*" class="hidden" @change="handleCoverFileUpload" />
        </label>
      </div>
    </div>

    <!-- Global Drag & Drop Overlay Indicator -->
    <div
      v-if="isDraggingFileOver"
      class="fixed inset-0 z-50 bg-brand-500/10 backdrop-blur-sm border-4 border-dashed border-brand-500 rounded-3xl m-6 flex flex-col items-center justify-center text-brand-600 dark:text-brand-400 pointer-events-none animate-pulse"
    >
      <UploadCloud class="w-16 h-16 mb-3 animate-bounce" />
      <h3 class="text-xl font-bold">Drop images to add to document</h3>
      <p class="text-xs text-slate-500 dark:text-slate-400 mt-1">Images will be uploaded and inserted as blocks automatically</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import draggable from 'vuedraggable'
import {
  GripVertical,
  Trash2,
  Plus,
  Maximize2,
  Minimize2,
  ImageIcon,
  Download,
  ChevronRight,
  MessageSquare,
  Sparkles,
  ChevronDown,
  Check,
  Upload,
  UploadCloud,
} from 'lucide-vue-next'
import SlashMenu from '@/components/editor/SlashMenu.vue'
import EditableBlock from '@/components/editor/EditableBlock.vue'
import CodeBlock from '@/components/editor/CodeBlock.vue'
import TableBlock from '@/components/editor/TableBlock.vue'
import TableOfContents from '@/components/editor/TableOfContents.vue'
import ImageBlock from '@/components/editor/ImageBlock.vue'
import NotionAIAssistant from '@/components/editor/NotionAIAssistant.vue'
import CommentsPanel from '@/components/editor/CommentsPanel.vue'
import FloatingToolbar from '@/components/editor/FloatingToolbar.vue'
import BlockActionsMenu from '@/components/editor/BlockActionsMenu.vue'
import { useWorkspaceStore } from '@/stores/workspace'
import { useAuthStore } from '@/stores/auth'
import wsService from '@/services/websocket'
import api from '@/services/api'
import type { Block, BlockType } from '@/types/workspace'

const workspaceStore = useWorkspaceStore()
const authStore = useAuthStore()

// Page Typography and Canvas Options
const fontStyle = ref<'sans' | 'serif' | 'mono'>('sans')
const isSmallText = ref(false)
const showFontMenu = ref(false)
const showAIModal = ref(false)
const showComments = ref(false)

const quickImageFileInputRef = ref<HTMLInputElement | null>(null)
const isDraggingFileOver = ref(false)

function onImageUrlChange(block: Block, url: string) {
  block.content.url = url
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, url }
  })
}

function onImageCaptionChange(block: Block, caption: string) {
  block.content.caption = caption
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, caption }
  })
}

function onImageWidthChange(block: Block, width: string) {
  block.content.width = width
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, width }
  })
}

function onImageAlignmentChange(block: Block, alignment: 'left' | 'center' | 'right') {
  block.content.alignment = alignment
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, alignment }
  })
}

function onImageAspectRatioChange(block: Block, ratio: string) {
  block.content.aspectRatio = ratio
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, aspectRatio: ratio }
  })
}

function onImageObjectPositionChange(block: Block, pos: string) {
  block.content.objectPosition = pos
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, objectPosition: pos }
  })
}

function triggerQuickImageUpload() {
  quickImageFileInputRef.value?.click()
}

async function handleQuickImageUpload(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files && files[0]) {
    await uploadAndInsertImage(files[0])
    if (quickImageFileInputRef.value) {
      quickImageFileInputRef.value.value = ''
    }
  }
}

async function uploadAndInsertImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  let uploadedUrl = ''
  try {
    const res = await api.post('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    if (res.data?.url) {
      uploadedUrl = res.data.url
    }
  } catch {
    // Graceful fallback to client Base64 URL
    uploadedUrl = await new Promise((resolve) => {
      const reader = new FileReader()
      reader.onload = () => resolve(reader.result as string)
      reader.readAsDataURL(file)
    })
  }

  if (uploadedUrl) {
    const created = await workspaceStore.createBlock({
      type: 'image',
      content: {
        url: uploadedUrl,
        caption: file.name.replace(/\.[^/.]+$/, ''),
        width: '100%',
        alignment: 'center',
      },
      position: draggableBlocks.value.length,
    })
    if (created) {
      focusBlock(created.id)
    }
  }
}

function handleDragOver(e: DragEvent) {
  if (e.dataTransfer?.types?.includes('Files')) {
    isDraggingFileOver.value = true
  }
}

function handleDragLeave(e: DragEvent) {
  if (e.clientX <= 0 || e.clientY <= 0 || e.clientX >= window.innerWidth || e.clientY >= window.innerHeight) {
    isDraggingFileOver.value = false
  }
}

async function handleDropFile(e: DragEvent) {
  isDraggingFileOver.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      if (file.type.startsWith('image/')) {
        await uploadAndInsertImage(file)
      }
    }
  }
}

async function handleGlobalPaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return

  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (file) {
        await uploadAndInsertImage(file)
      }
      return
    }
  }
}

async function handleCoverFileUpload(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files && files[0]) {
    const file = files[0]
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await api.post('/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      if (res.data?.url) {
        setCover(res.data.url)
      }
    } catch {
      const reader = new FileReader()
      reader.onload = () => setCover(reader.result as string)
      reader.readAsDataURL(file)
    }
  }
}

function onLanguageChange(block: Block, lang: string) {
  block.content.language = lang
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, language: lang }
  })
}

function onTableDataChange(block: Block, data: string[][]) {
  block.content.tableData = data
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, tableData: data }
  })
}

function onTableHeaderRowChange(block: Block, val: boolean) {
  block.content.hasHeaderRow = val
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, hasHeaderRow: val }
  })
}

function onTableHeaderColChange(block: Block, val: boolean) {
  block.content.hasHeaderCol = val
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, hasHeaderCol: val }
  })
}

const editorContainerRef = ref<HTMLElement | null>(null)
const pageTitle = ref(workspaceStore.currentPage?.title || '')
const isFullWidth = ref(false)

// Cover image state
const showCoverModal = ref(false)
const pageCoverUrl = ref<string | null>(null)
const customCoverInput = ref('')

const coverPresets = [
  'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1400&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1518770660439-4636190af475?w=1400&auto=format&fit=crop&q=80',
]

// Block 6-dot Context Menu state
const blockMenuOpen = ref(false)
const blockMenuPosition = ref({ x: 0, y: 0 })
const activeMenuBlock = ref<Block | null>(null)

// Slash menu state
const slashMenuOpen = ref(false)
const slashMenuPosition = ref({ x: 0, y: 0 })
const slashQuery = ref('')
const activeSlashBlockId = ref<string | null>(null)

// Watch active page title
watch(
  () => workspaceStore.currentPage,
  (newPage) => {
    if (newPage) {
      pageTitle.value = newPage.title
    }
  },
  { immediate: true }
)

const currentPage = computed(() => workspaceStore.currentPage)

// Computed word count
const wordCount = computed(() => {
  let count = 0
  workspaceStore.documentBlocks.forEach((b) => {
    if (b.content.text) {
      const words = b.content.text.trim().split(/\s+/).filter(Boolean)
      count += words.length
    }
  })
  return count
})

// Computed draggable blocks binding
const draggableBlocks = computed({
  get: () => workspaceStore.documentBlocks,
  set: (newOrder) => {
    const items = newOrder.map((b, idx) => ({
      id: b.id,
      parentId: null,
      position: idx,
    }))
    workspaceStore.batchMoveBlocks(items)
  },
})

function onTextChange(block: Block, text: string) {
  block.content.text = text
}

// Debounce timer map for typing
const typingTimers = new Map<string, any>()

function onInput(block: Block, event: Event) {
  const target = event.target as HTMLElement
  const text = target.innerText || ''

  // Slash command trigger detection
  if (text.startsWith('/')) {
    activeSlashBlockId.value = block.id
    slashQuery.value = text.slice(1)

    const rect = target.getBoundingClientRect()
    slashMenuPosition.value = {
      x: Math.min(rect.left, window.innerWidth - 280),
      y: rect.bottom + window.scrollY + 6,
    }
    slashMenuOpen.value = true
  } else {
    if (slashMenuOpen.value) {
      closeSlashMenu()
    }
  }

  // Update block content locally immediately
  block.content.text = text

  // Debounced STOMP + REST persistence
  if (typingTimers.has(block.id)) {
    clearTimeout(typingTimers.get(block.id))
  }

  const timer = setTimeout(() => {
    workspaceStore.updateBlock(block.id, {
      content: { ...block.content, text },
    })
    typingTimers.delete(block.id)
  }, 250)

  typingTimers.set(block.id, timer)
}

function onKeydown(block: Block, index: number, event: KeyboardEvent) {
  if (slashMenuOpen.value) {
    if (['ArrowDown', 'ArrowUp', 'Enter', 'Escape'].includes(event.key)) {
      return // Handled by SlashMenu
    }
  }

  if (event.key === 'Enter' && !event.shiftKey && block.type !== 'code') {
    event.preventDefault()
    addNewBlockAfter(index)
  } else if (event.key === 'Backspace') {
    const text = block.content.text || ''
    if (text === '') {
      event.preventDefault()
      removeBlock(block.id)
      focusPreviousBlock(index)
    }
  }
}

async function addNewBlockAfter(index: number) {
  const newPosition = index >= 0 ? index + 1 : 0
  const created = await workspaceStore.createBlock({
    type: 'paragraph',
    content: { text: '' },
    position: newPosition,
  })

  await nextTick()
  if (created) {
    focusBlock(created.id)
  }
}

function removeBlock(blockId: string) {
  workspaceStore.deleteBlock(blockId)
}

function toggleTodo(block: Block) {
  const newChecked = !block.content.checked
  block.content.checked = newChecked
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, checked: newChecked },
  })
}

function toggleExpand(block: Block) {
  const newOpen = !block.content.isOpen
  block.content.isOpen = newOpen
  workspaceStore.updateBlock(block.id, {
    content: { ...block.content, isOpen: newOpen },
  })
}

function changeImageUrl(block: Block) {
  const url = prompt('Enter new Image URL:', block.content.url || '')
  if (url) {
    block.content.url = url
    workspaceStore.updateBlock(block.id, {
      content: { ...block.content, url },
    })
  }
}

function handleSlashSelect(type: BlockType) {
  if (!activeSlashBlockId.value) return

  const block = workspaceStore.blocks.find((b) => b.id === activeSlashBlockId.value)
  if (block) {
    block.type = type
    block.content.text = ''
    if (type === 'image') {
      block.content.url = ''
    }

    workspaceStore.updateBlock(block.id, {
      type,
      content: { ...block.content, text: '', url: type === 'image' ? '' : block.content.url },
    })

    const el = document.getElementById(`block-${block.id}`)
    if (el) {
      el.innerText = ''
      el.focus()
    }
  }

  closeSlashMenu()
}

function closeSlashMenu() {
  slashMenuOpen.value = false
  activeSlashBlockId.value = null
  slashQuery.value = ''
}

// 6-dot handle block context menu
function openBlockMenu(block: Block, event: MouseEvent) {
  activeMenuBlock.value = block
  const target = event.currentTarget as HTMLElement
  const rect = target.getBoundingClientRect()
  blockMenuPosition.value = {
    x: Math.min(rect.right + 4, window.innerWidth - 240),
    y: rect.top + window.scrollY,
  }
  blockMenuOpen.value = true
}

function deleteActiveMenuBlock() {
  if (activeMenuBlock.value) {
    removeBlock(activeMenuBlock.value.id)
    blockMenuOpen.value = false
  }
}

async function duplicateActiveMenuBlock() {
  if (!activeMenuBlock.value || !currentPage.value) return
  const original = activeMenuBlock.value
  await workspaceStore.createBlock({
    type: original.type,
    content: { ...original.content },
    position: original.position + 1,
  })
  blockMenuOpen.value = false
}

function turnIntoType(type: BlockType) {
  if (!activeMenuBlock.value) return
  activeMenuBlock.value.type = type
  workspaceStore.updateBlock(activeMenuBlock.value.id, { type })
  blockMenuOpen.value = false
}

// Cover management
function setCover(url: string) {
  if (!url) return
  pageCoverUrl.value = url
  showCoverModal.value = false
  customCoverInput.value = ''
}

function removeCover() {
  pageCoverUrl.value = null
}

function focusBlock(blockId: string) {
  nextTick(() => {
    const el = document.getElementById(`block-${blockId}`)
    if (el) {
      el.focus()
      try {
        const selection = window.getSelection()
        const range = document.createRange()
        range.selectNodeContents(el)
        range.collapse(false)
        selection?.removeAllRanges()
        selection?.addRange(range)
      } catch (err) {
        // fallback
      }
    }
  })
}

function focusPreviousBlock(currentIndex: number) {
  if (currentIndex > 0) {
    const prevBlock = draggableBlocks.value[currentIndex - 1]
    if (prevBlock) {
      focusBlock(prevBlock.id)
    }
  }
}

async function focusFirstBlock() {
  if (draggableBlocks.value.length > 0) {
    focusBlock(draggableBlocks.value[0].id)
  } else {
    await addNewBlockAfter(-1)
  }
}

async function handleCanvasClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (target === editorContainerRef.value || target.closest('.canvas-empty-area')) {
    if (draggableBlocks.value.length === 0) {
      await addNewBlockAfter(-1)
    } else {
      const lastBlock = draggableBlocks.value[draggableBlocks.value.length - 1]
      if (!lastBlock.content.text || lastBlock.content.text.trim() === '') {
        focusBlock(lastBlock.id)
      } else {
        await addNewBlockAfter(draggableBlocks.value.length - 1)
      }
    }
  }
}

function onDragEnd() {
  const items = draggableBlocks.value.map((b, idx) => ({
    id: b.id,
    parentId: null,
    position: idx,
  }))
  workspaceStore.batchMoveBlocks(items)
}

function savePageTitle() {
  if (currentPage.value && pageTitle.value !== currentPage.value.title) {
    workspaceStore.updatePage(currentPage.value.id, {
      title: pageTitle.value || 'Untitled',
    })
  }
}

function changeIcon() {
  const emojis = ['🚀', '📄', '💡', '📊', '🔥', '🎯', '✨', '⚡', '💻', '📝', '🧠', '🛠️']
  const currentIcon = currentPage.value?.icon || '📄'
  const nextIdx = (emojis.indexOf(currentIcon) + 1) % emojis.length
  const nextEmoji = emojis[nextIdx]

  if (currentPage.value) {
    workspaceStore.updatePage(currentPage.value.id, { icon: nextEmoji })
  }
}

// Export Document as Markdown file
function exportMarkdown() {
  let md = `# ${pageTitle.value || 'Untitled'}\n\n`
  draggableBlocks.value.forEach((b) => {
    const text = b.content.text || ''
    switch (b.type) {
      case 'heading_1': md += `# ${text}\n\n`; break
      case 'heading_2': md += `## ${text}\n\n`; break
      case 'heading_3': md += `### ${text}\n\n`; break
      case 'todo': md += `- [${b.content.checked ? 'x' : ' '}] ${text}\n`; break
      case 'bullet_list': md += `- ${text}\n`; break
      case 'numbered_list': md += `1. ${text}\n`; break
      case 'quote': md += `> ${text}\n\n`; break
      case 'code': md += `\`\`\`${b.content.language || 'typescript'}\n${text}\n\`\`\`\n\n`; break
      case 'callout': md += `> 💡 **Note:** ${text}\n\n`; break
      case 'divider': md += `---\n\n`; break
      case 'image': md += `![${b.content.caption || 'Image'}](${b.content.url})\n\n`; break
      default: md += `${text}\n\n`; break
    }
  })

  const blob = new Blob([md], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${(pageTitle.value || 'workspace-document').toLowerCase().replace(/\s+/g, '-')}.md`
  a.click()
  URL.revokeObjectURL(url)
}

// Broadcast live cursor movements across page
function handleMouseMove(e: MouseEvent) {
  if (!currentPage.value || !authStore.user) return

  wsService.sendCursorPosition(currentPage.value.id, {
    userId: authStore.user.id,
    userName: authStore.user.name,
    userAvatar: authStore.user.avatarUrl,
    x: e.clientX,
    y: e.clientY,
    color: authStore.cursorColor,
  })
}

onMounted(() => {
  window.addEventListener('paste', handleGlobalPaste)
  window.addEventListener('dragover', handleDragOver)
  window.addEventListener('dragleave', handleDragLeave)
  window.addEventListener('drop', handleDropFile)
})

onUnmounted(() => {
  window.removeEventListener('paste', handleGlobalPaste)
  window.removeEventListener('dragover', handleDragOver)
  window.removeEventListener('dragleave', handleDragLeave)
  window.removeEventListener('drop', handleDropFile)
})
</script>
