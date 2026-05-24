# Markdown HTML 样式参考

本文档描述 Markdown 内容转换后的 HTML 结构，供前端自定义样式参考。

## 处理流程

```
Markdown → Goldmark 解析 → Bluemonday 安全过滤 → HTML 输出
```

## 支持的 Markdown 语法

### 1. 标题

| Markdown | HTML |
|----------|------|
| `# H1` | `<h1 id="标题">标题</h1>` |
| `## H2` | `<h2 id="标题">标题</h2>` |
| `### H3` | `<h3 id="标题">标题</h3>` |

### 2. 任务列表

**Markdown：**
```markdown
- [x] 已完成的任务
- [ ] 未完成的任务
```

**HTML：**
```html
<ul>
  <li>
    <input checked="" disabled="" type="checkbox"/> 已完成的任务
  </li>
  <li>
    <input disabled="" type="checkbox"/> 未完成的任务
  </li>
</ul>
```

### 3. 表格

**Markdown：**
```markdown
| 表头1 | 表头2 |
|-------|-------|
| 内容1 | 内容2 |
| 内容3 | 内容4 |
```

**HTML：**
```html
<table>
  <thead>
    <tr>
      <th>表头1</th>
      <th>表头2</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>内容1</td>
      <td>内容2</td>
    </tr>
    <tr>
      <td>内容3</td>
      <td>内容4</td>
    </tr>
  </tbody>
</table>
```

### 4. 删除线

**Markdown：**
```markdown
~~删除的文字~~
```

**HTML：**
```html
<p><del>删除的文字</del></p>
```

### 5. 其他标准语法

| Markdown | HTML |
|----------|------|
| `**粗体**` | `<strong>粗体</strong>` |
| `*斜体*` | `<em>斜体</em>` |
| `` `行内代码` `` | `<code>行内代码</code>` |
| `[链接](url)` | `<a href="url">链接</a>` |
| `![图片](url)` | `<img src="url" data-preview-src="url"/>` |
| `> 引用` | `<blockquote><p>引用</p></blockquote>` |
| `- 无序列表` | `<ul><li>无序列表</li></ul>` |
| `1. 有序列表` | `<ol><li>有序列表</li></ol>` |
| ```` ```代码块``` ```` | `<pre><code>代码块</code></pre>` |

### 6. 图片大图预览

**功能说明：**
只有 `*.likofan` 域名的图片会被保留，并自动添加 `data-preview-src` 属性用于大图预览功能。其他域名的图片会被过滤移除。

**Markdown：**
```markdown
![描述](https://img.likofan.com/image.jpg)
```

**HTML：**
```html
<img src="https://img.likofan.com/image.jpg" data-preview-src="https://img.likofan.com/image.jpg"/>
```

**前端实现示例：**

```javascript
// 使用图片预览库（如 viewer.js）
document.querySelectorAll('.markdown-content img').forEach(img => {
  if (img.dataset.previewSrc) {
    img.style.cursor = 'zoom-in';
    img.addEventListener('click', () => {
      // 触发大图预览
      const previewWindow = window.open(img.dataset.previewSrc, '_blank', 'width=800,height=600');
    });
  }
});

// 或使用 viewer.js
import 'viewerjs/dist/viewer.min.css';
import Viewer from 'viewerjs';

const viewer = new Viewer(document.querySelector('.markdown-content'), {
  url: 'data-preview-src',
});
```

## CSS 样式参考

### 基础样式

```css
/* 容器 */
.markdown-content {
  line-height: 1.6;
  word-wrap: break-word;
}

/* 标题 */
.markdown-content h1,
.markdown-content h2,
.markdown-content h3 {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
}

.markdown-content h1 { font-size: 1.8em; }
.markdown-content h2 { font-size: 1.5em; }
.markdown-content h3 { font-size: 1.25em; }
```

### 任务列表样式

```css
/* 任务列表容器 */
.markdown-content ul.contains-task-list {
  list-style: none;
  padding-left: 0;
}

/* 任务列表项 */
.markdown-content li.task-list-item {
  display: flex;
  align-items: flex-start;
  gap: 0.5em;
}

/* Checkbox 美化（可选） */
.markdown-content .task-list-item-checkbox {
  margin-top: 0.3em;
  width: 16px;
  height: 16px;
  accent-color: #4F46E5; /* 自定义颜色 */
}

/* 已完成状态文字变灰 */
.markdown-content li.task-list-item input[checked] + * {
  color: #999;
  text-decoration: line-through;
}
```

### 表格样式

```css
/* 表格容器 */
.markdown-content table {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
}

/* 表头 */
.markdown-content th {
  background-color: #f5f5f5;
  font-weight: 600;
  text-align: left;
}

/* 单元格 */
.markdown-content th,
.markdown-content td {
  padding: 0.75em;
  border: 1px solid #e0e0e0;
}

/* 斑马纹（可选） */
.markdown-content tr:nth-child(even) {
  background-color: #fafafa;
}
```

### 删除线样式

```css
.markdown-content del {
  color: #999;
  text-decoration: line-through;
}
```

### 引用样式

```css
.markdown-content blockquote {
  margin: 1em 0;
  padding: 0.5em 1em;
  border-left: 4px solid #4F46E5;
  background-color: #f9f9f9;
  color: #666;
}

.markdown-content blockquote p {
  margin: 0;
}
```

### 代码块样式

```css
.markdown-content pre {
  background-color: #f5f5f5;
  padding: 1em;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-content code {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.9em;
}
```

## 完整示例

### 输入 Markdown

```markdown
# 标题

这是一个包含多种语法的示例：

## 任务列表
- [x] 已完成任务
- [ ] 待办任务

## 表格
| 功能 | 状态 |
|------|------|
| 表格 | ✅ |
| 删除线 | ~~已完成~~ |

## 引用
> 这是引用内容
```

### 输出 HTML

```html
<h1 id="标题">标题</h1>
<p>这是一个包含多种语法的示例：</p>

<h2 id="任务列表">任务列表</h2>
<ul>
  <li>
    <input checked="" disabled="" type="checkbox"/> 已完成任务
  </li>
  <li>
    <input disabled="" type="checkbox"/> 待办任务
  </li>
</ul>

<h2 id="表格">表格</h2>
<table>
  <thead>
    <tr><th>功能</th><th>状态</th></tr>
  </thead>
  <tbody>
    <tr><td>表格</td><td>✅</td></tr>
    <tr><td>删除线</td><td><del>已完成</del></td></tr>
  </tbody>
</table>

<h2 id="引用">引用</h2>
<blockquote><p>这是引用内容</p></blockquote>
```