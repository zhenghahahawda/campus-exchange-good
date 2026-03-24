<template>
  <div class="theme-manage-page">
    <div class="page-header-wrapper">
      <PageHeader title="主题管理" description="管理系统主题配置，支持新增、编辑、启用/禁用主题" />
      <div class="header-actions">
        <button class="glass-btn btn-primary" @click="handleAdd"><i class="el-icon-plus"></i><span>新建主题</span></button>
      </div>
    </div>
    <div class="row section-container">
      <div class="col-xl-4 col-md-6" @click="handleStatClick('all')"><StatCard title="全部主题" :value="statAll" icon="el-icon-brush" color="#0075ff" :class="{ 'active-stat-card': filterStatus === 'all' }" /></div>
      <div class="col-xl-4 col-md-6" @click="handleStatClick(1)"><StatCard title="已启用" :value="statActive" icon="el-icon-circle-check" color="#2dce89" :class="{ 'active-stat-card': filterStatus === 1 }" /></div>
      <div class="col-xl-4 col-md-6" @click="handleStatClick(0)"><StatCard title="已禁用" :value="statInactive" icon="el-icon-remove-outline" color="#fb6340" :class="{ 'active-stat-card': filterStatus === 0 }" /></div>
    </div>
    <div class="filter-bar">
      <div class="search-wrapper"><el-input placeholder="搜索主题名称/标识/描述..." v-model="searchQuery" prefix-icon="el-icon-search" clearable class="capsule-input" @clear="handleSearch" @keyup.enter.native="handleSearch" /></div>
      <div class="sort-wrapper"><el-button type="primary" icon="el-icon-refresh" circle @click="fetchThemes" class="glass-btn-icon" /></div>
    </div>
    <div v-if="filteredList.length > 0" class="theme-table-wrapper glass-card">
      <el-table :data="pagedList" style="width: 100%" class="glass-table">
        <el-table-column label="预览" width="80">
          <template slot-scope="{ row }"><div class="color-preview" :style="{ background: row.gradient || row.primaryColor }"></div></template>
        </el-table-column>
        <el-table-column prop="themeKey" label="标识" width="150" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="分类" width="160">
          <template slot-scope="{ row }"><el-tag v-for="cat in parseJson(row.category)" :key="cat" size="mini" style="margin: 2px;">{{ cat }}</el-tag></template>
        </el-table-column>
        <el-table-column label="壁纸" width="80" align="center">
          <template slot-scope="{ row }">
            <el-tag v-if="row.wallpaperType === 'video'" size="mini" type="danger">视频</el-tag>
            <el-tag v-else-if="row.wallpaperType === 'image'" size="mini" type="warning">图片</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" sortable />
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="{ row }"><el-tag :type="row.isActive === 1 ? 'success' : 'info'" size="small">{{ row.isActive === 1 ? '启用' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" :icon="row.isActive === 1 ? 'el-icon-switch-button' : 'el-icon-video-play'" :class="row.isActive === 1 ? 'text-warning' : 'text-success'" :disabled="['default', 'ios-style'].includes(row.themeKey)" @click="handleToggle(row)">{{ row.isActive === 1 ? '禁用' : '启用' }}</el-button>
            <el-button type="text" icon="el-icon-edit" class="text-primary" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" class="text-danger" :disabled="['default', 'ios-style'].includes(row.themeKey)" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <EmptyState v-else icon="el-icon-brush" message="暂无符合条件的主题" action-text="清除筛选条件" :show-action="true" @action="handleClearFilters" />
    <div v-if="filteredList.length > 0" class="pagination-wrapper">
      <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="filteredList.length" :page-sizes="[10, 20, 50]" :page-size="pageSize" :current-page="currentPage" @current-change="handlePageChange" @size-change="handleSizeChange" class="glass-pagination" />
    </div>
    <el-dialog :title="isEdit ? '编辑主题' : '新建主题'" :visible.sync="dialogVisible" width="800px" top="5vh" custom-class="glass-dialog" :close-on-click-modal="false" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" class="glass-form">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="主题标识" prop="themeKey"><el-input v-model="form.themeKey" :disabled="isEdit" placeholder="如 dark-gold" /></el-form-item>
        <el-form-item label="主题名称" prop="name"><el-input v-model="form.name" placeholder="如 暗黑金主题" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" placeholder="主题描述" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="categoryArr" multiple placeholder="选择分类"><el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" /></el-select></el-form-item>
        <el-form-item label="标签"><el-select v-model="tagsArr" multiple filterable allow-create placeholder="输入标签后回车" /></el-form-item>
        <el-form-item label="排序"><el-input v-model.number="form.sortOrder" type="number" placeholder="排序值" /></el-form-item>
        <el-divider content-position="left">配色方案</el-divider>
        <el-form-item label="预览渐变"><el-input v-model="form.gradient" placeholder="linear-gradient(135deg, #0A0A0A 0%, #1A1A1A 100%)" /></el-form-item>
        <el-form-item label="主色"><div style="display:flex;align-items:center;gap:8px;"><el-color-picker v-model="form.primaryColor" show-alpha /><el-input v-model="form.primaryColor" size="small" style="width:140px;" /></div></el-form-item>
        <el-form-item label="侧边栏背景"><el-input v-model="form.sidebarBg" placeholder="rgba(26, 26, 26, 0.7)" /></el-form-item>
        <el-divider content-position="left">CSS 变量</el-divider>
        <div class="css-vars-editor">
          <div v-for="(item, idx) in cssVarsList" :key="idx" class="css-var-row">
            <el-input v-model="item.key" placeholder="--color-primary" size="small" style="width:220px;" />
            <el-input v-model="item.value" placeholder="#D4AF37" size="small" style="width:240px;margin-left:8px;" />
            <el-button icon="el-icon-delete" size="mini" circle style="margin-left:8px;" @click="cssVarsList.splice(idx, 1)" />
          </div>
          <el-button size="small" icon="el-icon-plus" @click="cssVarsList.push({ key: '', value: '' })">添加变量</el-button>
        </div>
        <el-divider content-position="left">壁纸</el-divider>
        <el-form-item label="壁纸类型"><el-radio-group v-model="form.wallpaperType"><el-radio :label="null">无</el-radio><el-radio label="image">图片</el-radio><el-radio label="video">视频</el-radio></el-radio-group></el-form-item>
        <el-form-item v-if="form.wallpaperType" label="壁纸文件">
          <el-upload action="" :http-request="uploadWallpaperFile" :show-file-list="false" :accept="form.wallpaperType === 'video' ? 'video/mp4' : 'image/*'"><el-button size="small" type="primary">上传{{ form.wallpaperType === 'video' ? '视频' : '图片' }}</el-button></el-upload>
          <div v-if="form.wallpaperUrl" style="margin-top:8px;font-size:12px;color:#999;word-break:break-all;">{{ form.wallpaperUrl }}</div>
        </el-form-item>
        <el-form-item label="预览图">
          <el-upload action="" :http-request="uploadPreviewFile" :show-file-list="false" accept="image/*"><el-button size="small">上传预览图</el-button></el-upload>
          <img v-if="form.previewImageUrl" :src="form.previewImageUrl" style="max-height:60px;margin-top:8px;border-radius:4px;" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" class="glass-btn-cancel">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="glass-btn-confirm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import { MessageHelper, ConfirmHelper } from '@/utils/messageHelper'
import { getAllThemes, createTheme, updateTheme, deleteTheme, toggleTheme, uploadWallpaper, uploadPreview } from '@/api/theme'

export default {
  name: 'ThemeManagePage',
  middleware: 'admin-only',
  components: { PageHeader, EmptyState, StatCard },
  data() {
    return {
      allThemes: [],
      searchQuery: '',
      filterStatus: 'all',
      currentPage: 1,
      pageSize: 50,
      dialogVisible: false,
      isEdit: false,
      editId: null,
      submitLoading: false,
      categoryOptions: ['dark', 'light', 'tech', 'nature', 'luxury', 'static', 'video', 'creative', 'culture'],
      categoryArr: [],
      tagsArr: [],
      cssVarsList: [],
      form: { themeKey: '', name: '', description: '', gradient: '', primaryColor: '#0075ff', sidebarBg: '', cssVariables: '{}', wallpaperUrl: null, wallpaperType: null, previewImageUrl: null, sortOrder: 0 },
      rules: {
        themeKey: [{ required: true, message: '请输入主题标识', trigger: 'blur' }, { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和连字符', trigger: 'blur' }],
        name: [{ required: true, message: '请输入主题名称', trigger: 'blur' }]
      }
    }
  },
  computed: {
    statAll() { return this.allThemes.length },
    statActive() { return this.allThemes.filter(t => t.isActive === 1).length },
    statInactive() { return this.allThemes.filter(t => t.isActive !== 1).length },
    filteredList() {
      let list = this.allThemes
      if (this.filterStatus !== 'all') list = list.filter(t => t.isActive === this.filterStatus)
      if (this.searchQuery.trim()) {
        const q = this.searchQuery.toLowerCase()
        list = list.filter(t => (t.themeKey && t.themeKey.toLowerCase().includes(q)) || (t.name && t.name.toLowerCase().includes(q)) || (t.description && t.description.toLowerCase().includes(q)))
      }
      return list
    },
    pagedList() { const s = (this.currentPage - 1) * this.pageSize; return this.filteredList.slice(s, s + this.pageSize) }
  },
  created() { this.fetchThemes() },
  watch: { searchQuery(v) { if (!v) this.handleSearch() } },
  methods: {
    async fetchThemes() {
      try { const res = await getAllThemes(this.$axios); this.allThemes = res?.data?.data || res?.data || [] }
      catch (e) { MessageHelper.error(this, '获取主题列表失败') }
    },
    parseJson(str) { if (!str) return []; if (Array.isArray(str)) return str; try { return JSON.parse(str) } catch (e) { return [] } },
    handleStatClick(status) { this.filterStatus = status; this.currentPage = 1 },
    handleSearch() { this.currentPage = 1 },
    handlePageChange(page) { this.currentPage = page; window.scrollTo({ top: 0, behavior: 'smooth' }) },
    handleSizeChange(size) { this.pageSize = size; this.currentPage = 1 },
    handleClearFilters() { this.searchQuery = ''; this.filterStatus = 'all'; this.currentPage = 1 },
    handleAdd() {
      this.isEdit = false; this.editId = null
      this.form = { themeKey: '', name: '', description: '', gradient: '', primaryColor: '#0075ff', sidebarBg: '', cssVariables: '{}', wallpaperUrl: null, wallpaperType: null, previewImageUrl: null, sortOrder: 0 }
      this.categoryArr = []; this.tagsArr = []
      this.cssVarsList = [{ key: '--color-primary', value: '' },{ key: '--color-primary-rgb', value: '' },{ key: '--color-bg-page', value: '' },{ key: '--color-bg-surface', value: '' },{ key: '--color-border', value: '' },{ key: '--color-text-primary', value: '' },{ key: '--color-text-secondary', value: '' }]
      this.dialogVisible = true
      this.$nextTick(() => { this.$refs.form && this.$refs.form.clearValidate() })
    },
    handleEdit(row) {
      this.isEdit = true; this.editId = row.themeId
      this.form = { themeKey: row.themeKey, name: row.name, description: row.description || '', gradient: row.gradient || '', primaryColor: row.primaryColor || '#0075ff', sidebarBg: row.sidebarBg || '', cssVariables: row.cssVariables || '{}', wallpaperUrl: row.wallpaperUrl, wallpaperType: row.wallpaperType, previewImageUrl: row.previewImageUrl, sortOrder: row.sortOrder || 0 }
      this.categoryArr = this.parseJson(row.category); this.tagsArr = this.parseJson(row.tags)
      let vars = {}; try { vars = JSON.parse(row.cssVariables || '{}') } catch (e) {}
      this.cssVarsList = Object.entries(vars).map(([key, value]) => ({ key, value }))
      this.dialogVisible = true
      this.$nextTick(() => { this.$refs.form && this.$refs.form.clearValidate() })
    },
    async handleSubmit() {
      try { await this.$refs.form.validate() } catch (e) { return }
      const validVars = this.cssVarsList.filter(v => v.key && v.value)
      if (validVars.length === 0) { MessageHelper.warning(this, '请至少添加一个CSS变量'); return }
      const cssObj = {}; validVars.forEach(v => { cssObj[v.key.trim()] = v.value.trim() })
      const data = { ...this.form, category: JSON.stringify(this.categoryArr), tags: JSON.stringify(this.tagsArr), cssVariables: JSON.stringify(cssObj) }
      this.submitLoading = true
      try {
        if (this.isEdit) { await updateTheme(this.$axios, this.editId, data); MessageHelper.success(this, '更新成功') }
        else { await createTheme(this.$axios, data); MessageHelper.success(this, '创建成功') }
        this.dialogVisible = false; this.fetchThemes()
      } catch (e) { MessageHelper.error(this, e?.response?.data?.message || '操作失败') }
      finally { this.submitLoading = false }
    },
    async handleToggle(row) {
      try {
        const txt = row.isActive === 1 ? '禁用' : '启用'
        await ConfirmHelper.confirm(this, '确认' + txt + '该主题吗？', '主题: ' + row.name)
        await toggleTheme(this.$axios, row.themeId); MessageHelper.success(this, txt + '成功'); this.fetchThemes()
      } catch (e) { if (e !== 'cancel') MessageHelper.error(this, '操作失败') }
    },
    async handleDelete(row) {
      try {
        await ConfirmHelper.confirmDelete(this, '主题: ' + row.name)
        await deleteTheme(this.$axios, row.themeId); MessageHelper.success(this, '删除成功')
        if (this.pagedList.length === 1 && this.currentPage > 1) this.currentPage--
        this.fetchThemes()
      } catch (e) { if (e !== 'cancel') MessageHelper.error(this, e?.response?.data?.message || '删除失败') }
    },
    async uploadWallpaperFile({ file }) {
      const fd = new FormData(); fd.append('file', file)
      try { const res = await uploadWallpaper(this.$axios, fd); this.form.wallpaperUrl = res?.data?.data || res?.data; MessageHelper.success(this, '壁纸上传成功') }
      catch (e) { MessageHelper.error(this, e?.response?.data?.message || '上传失败') }
    },
    async uploadPreviewFile({ file }) {
      const fd = new FormData(); fd.append('file', file)
      try { const res = await uploadPreview(this.$axios, fd); this.form.previewImageUrl = res?.data?.data || res?.data; MessageHelper.success(this, '预览图上传成功') }
      catch (e) { MessageHelper.error(this, e?.response?.data?.message || '上传失败') }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/css/themes/index.scss';

.theme-manage-page { padding: 10px 20px; min-height: 100vh; color: $color-text-primary; }

.page-header-wrapper {
  display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 0px;
  .header-actions { display: flex; gap: 12px; }
  .btn-primary {
    display: flex; align-items: center; gap: 8px; padding: 12px 24px; border: none; border-radius: 12px;
    font-size: 14px; font-weight: 600; cursor: pointer;
    background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.95), rgba(var(--color-primary-rgb), 0.85));
    border: 1px solid rgba(var(--color-primary-rgb), 0.3); color: #fff;
    backdrop-filter: blur(10px); box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); outline: none;
    i { font-size: 16px; font-weight: bold; }
    &:hover { background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 1), rgba(var(--color-primary-rgb), 0.9)); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(var(--color-primary-rgb), 0.4); }
    &:active { transform: translateY(0); }
  }
}

.section-container { margin-bottom: 32px; }
.row { display: flex; flex-wrap: wrap; margin: 0 -10px; }
.col-xl-4, .col-md-6 { width: 100%; padding: 0 10px; cursor: pointer; }
@media (min-width: 768px) { .col-md-6 { flex: 0 0 50%; max-width: 50%; } }
@media (min-width: 1200px) { .col-xl-4 { flex: 0 0 33.333%; max-width: 33.333%; } }

.active-stat-card {
  transform: translateY(-4px) !important;
  box-shadow: 0 8px 25px rgba(var(--color-primary-rgb), 0.25) !important;
  border: 2px solid rgba(var(--color-primary-rgb), 0.6) !important;
  background: rgba(var(--color-primary-rgb), 0.08) !important;
  position: relative;
  &::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 40%; height: 3px; background: linear-gradient(90deg, transparent, rgba(var(--color-primary-rgb), 0.8), transparent); border-radius: 3px; }
}

.filter-bar {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;
  .search-wrapper { flex: 1; max-width: 400px; }
  .glass-btn-icon { background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); color: $color-text-primary; &:hover { background: $color-primary; border-color: $color-primary; color: #fff; } }
}

.theme-table-wrapper { border-radius: 20px; overflow: hidden; margin-bottom: 24px; @include glass-card; }
.color-preview { width: 40px; height: 28px; border-radius: 6px; border: 1px solid rgba(255,255,255,0.1); }

.glass-table {
  background: transparent !important;
  ::v-deep {
    th.el-table__cell { background: rgba(255,255,255,0.05) !important; color: $color-text-primary !important; font-weight: 600; border-bottom: 1px solid $color-border !important; }
    td.el-table__cell { background: transparent !important; color: $color-text-secondary !important; border-bottom: 1px solid $color-border !important; }
    .el-table__row:hover > td.el-table__cell { background: rgba(var(--color-primary-rgb), 0.05) !important; }
    &.el-table { background-color: transparent !important; }
    .el-table__header-wrapper, .el-table__footer-wrapper { background-color: transparent !important; }
    .el-table__header tr { background-color: transparent !important; }
    .el-table__row { background-color: transparent !important; }
    .el-table__empty-block { background-color: transparent !important; }
    .el-table__empty-text { color: $color-text-disabled !important; }
    &::before { display: none; }
    .el-tag { border: none; border-radius: 20px; padding: 0 16px; height: 28px; line-height: 28px; font-weight: 500; font-size: 12px;
      &.el-tag--success { background-color: rgba(var(--color-secondary-success-rgb), 0.15); color: var(--color-secondary-success); }
      &.el-tag--info { background-color: rgba(var(--color-secondary-info-rgb), 0.15); color: var(--color-secondary-info); }
      &.el-tag--warning { background-color: rgba(var(--color-secondary-warning-rgb), 0.15); color: var(--color-secondary-warning); }
      &.el-tag--danger { background-color: rgba(var(--color-secondary-danger-rgb), 0.15); color: var(--color-secondary-danger); }
    }
  }
}

.text-primary { color: $color-primary; &:hover { opacity: 0.8; } }
.text-success { color: #67c23a; &:hover { color: #85ce61; } }
.text-warning { color: #e6a23c; &:hover { color: #ebb563; } }
.text-danger { color: #ff4d4f; &:hover { color: #ff7875; } }

.css-vars-editor { margin-bottom: 16px; }
.css-var-row { display: flex; align-items: center; margin-bottom: 6px; }

::v-deep {
  .capsule-input .el-input__inner { border-radius: 50px; background: var(--color-pill-bg); border: 1px solid rgba(255,255,255,0.1); height: 46px; color: $color-text-primary; &:focus { border-color: $color-primary; background: var(--color-bg-surface); } }
  .glass-dialog {
    background: var(--color-bg-surface) !important; border-radius: 20px !important; box-shadow: 0 10px 30px rgba(0,0,0,0.2) !important; border: 1px solid var(--color-border); @include glass-effect;
    .el-dialog__header { border-bottom: 1px solid var(--color-border); padding: 20px 24px; .el-dialog__title { color: $color-text-primary; font-weight: 600; } }
    .el-dialog__body { padding: 24px; color: $color-text-primary; max-height: 65vh; overflow-y: auto; }
    .el-dialog__footer { border-top: 1px solid var(--color-border); padding: 16px 24px; }
    .el-form-item__label { color: $color-text-primary; }
    .el-input__inner { background: rgba(255,255,255,0.05); border-color: var(--color-border); color: $color-text-primary; }
    .el-divider__text { color: $color-text-secondary; background-color: var(--color-bg-surface); }
    .el-divider { border-top-color: var(--color-border); }
    .el-select { width: 100%; }
    .el-radio__label { color: $color-text-primary; }
  }
}

.pagination-wrapper { display: flex; justify-content: center; margin-top: 20px; }
@import '@/assets/css/components/pagination.scss';
</style>
