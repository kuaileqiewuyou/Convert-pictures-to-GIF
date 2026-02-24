const form = document.getElementById('gifForm');
const filesInput = document.getElementById('filesInput');
const fileHint = document.getElementById('fileHint');
const dropzone = document.getElementById('dropzone');
const delayMsInput = document.getElementById('delayMs');
const loopInput = document.getElementById('loop');
const submitBtn = document.getElementById('submitBtn');
const statusText = document.getElementById('status');
const resultBox = document.getElementById('result');
const preview = document.getElementById('preview');
const downloadLink = document.getElementById('downloadLink');

let latestObjectUrl = null;

filesInput.addEventListener('change', () => {
  const count = filesInput.files ? filesInput.files.length : 0;

  if (count === 0) {
    fileHint.textContent = '点击或拖拽图片到这里（至少 2 张）';
    dropzone.classList.remove('selected');
    setStatus('');
    return;
  }

  fileHint.textContent = '已选择 ' + count + ' 张图片';
  dropzone.classList.add('selected');
  setStatus('已选择 ' + count + ' 张图片，点击“生成 GIF”开始处理。');
});

form.addEventListener('submit', async (e) => {
  e.preventDefault();

  const files = filesInput.files;
  if (!files || files.length < 2) {
    setStatus('请至少选择 2 张图片。', true);
    return;
  }

  const formData = new FormData();
  Array.from(files).forEach((file) => formData.append('files', file));
  formData.append('delayMs', delayMsInput.value || '300');
  formData.append('loop', String(loopInput.checked));

  submitBtn.disabled = true;
  setStatus('正在生成 GIF，请稍候...');

  try {
    const res = await fetch('/api/gif/create', {
      method: 'POST',
      body: formData,
    });

    if (!res.ok) {
      let message = '请求失败';
      const contentType = res.headers.get('content-type') || '';
      if (contentType.includes('application/json')) {
        const payload = await res.json();
        message = payload.message || payload.error || message;
      } else {
        const text = await res.text();
        if (text) message = text;
      }
      throw new Error(message);
    }

    const blob = await res.blob();
    if (latestObjectUrl) {
      URL.revokeObjectURL(latestObjectUrl);
    }
    latestObjectUrl = URL.createObjectURL(blob);

    preview.src = latestObjectUrl;
    downloadLink.href = latestObjectUrl;
    resultBox.classList.remove('hidden');
    setStatus('生成成功，可在下方预览并下载。');
  } catch (err) {
    setStatus('生成失败：' + err.message, true);
  } finally {
    submitBtn.disabled = false;
  }
});

function setStatus(message, isError = false) {
  statusText.textContent = message;
  statusText.style.color = isError ? '#b4331c' : '#1d5e5e';
}