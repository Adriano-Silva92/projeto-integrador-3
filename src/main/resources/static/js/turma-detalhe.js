
const turmaId = document.body.dataset.turmaId;

    // SWEETALERT HELP
    function showToast(message, icon = 'success') {
        const Toast = Swal.mixin({
            toast: true,
            position: 'bottom-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true,
            didOpen: (toast) => {
                toast.addEventListener('mouseenter', Swal.stopTimer);
                toast.addEventListener('mouseleave', Swal.resumeTimer);
            }
        });
        Toast.fire({ icon: icon, title: message });
    }

    function showAlert(title, message, icon = 'info') {
        Swal.fire({
            title: title,
            text: message,
            icon: icon,
            confirmButtonColor: '#4361ee',
            confirmButtonText: 'OK',
            background: document.body.classList.contains('dark-theme') ? '#1e1e2e' : '#fff',
            color: document.body.classList.contains('dark-theme') ? '#e0e0e0' : '#333'
        });
    }

    // REMOVER ALUNO
    function confirmarRemocaoAluno(alunoTurmaId) {
        Swal.fire({
            title: 'Remover aluno?',
            text: 'Deseja realmente remover este aluno da turma?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#dc3545',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sim, remover',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                // CRIAR DINAMICAMENTE
                const form = document.createElement('form');
                form.method = 'post';
                form.action = `/turmas/${turmaId}/remover-aluno/${alunoTurmaId}`;
                document.body.appendChild(form);
                form.submit();
            }
        });
    }
    
    // BUSCAR FALTAS   
    function verFaltasAlunoFromBtn(btn) {
        const alunoId = btn.getAttribute("data-id");
        const alunoNome = btn.getAttribute("data-nome");

        verFaltasAluno(alunoId, alunoNome);
    }

    // VER FALTAS DO ALUNO
    async function verFaltasAluno(alunoId, alunoNome) {
        document.getElementById('nomeAlunoFaltas').innerText = alunoNome;
        const modalBody = document.getElementById('modalFaltasBody');
        modalBody.innerHTML = `
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Carregando...</span>
                </div>
                <p class="mt-2">Carregando histórico de faltas...</p>
            </div>
        `;
        
        new bootstrap.Modal(document.getElementById('verFaltasModal')).show();
        
        try {
            const response = await fetch(`/frequencia/aluno/${alunoId}/turma/${turmaId}/faltas`);
            if (response.ok) {
                const faltas = await response.json();
                if (faltas.length === 0) {
                    modalBody.innerHTML = `
                        <div class="text-center py-4">
                            <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
                            <p class="mb-0">Este aluno não possui faltas registradas!</p>
                        </div>
                    `;
                } else {
                    let html = `
                        <div class="list-group">
                            <div class="list-group-item bg-light fw-bold">
                                <div class="row">
                                    <div class="col-4">Data</div>
                                    <div class="col-6">Observação</div>
                                    <div class="col-2">Status</div>
                                </div>
                            </div>
                    `;
                    faltas.forEach(falta => {
                        html += `
                            <div class="list-group-item">
                                <div class="row align-items-center">
                                    <div class="col-4">
                                        <i class="fas fa-calendar-alt me-2 text-danger"></i>
										<strong>${falta.data.split('-').reverse().join('/')}</strong>
                                    </div>
                                    <div class="col-6">${falta.observacao || '-'}</div>
                                    <div class="col-2">
                                        <span class="badge-status badge-falta">Falta</span>
                                    </div>
                                </div>
                            </div>
                        `;
                    });
                    html += `</div>`;
                    modalBody.innerHTML = html;
                }
            } else {
                modalBody.innerHTML = `
                    <div class="text-center py-4">
                        <i class="fas fa-exclamation-triangle fa-3x text-warning mb-3"></i>
                        <p class="mb-0">Erro ao carregar faltas do aluno.</p>
                    </div>
                `;
            }
        } catch (error) {
            modalBody.innerHTML = `
                <div class="text-center py-4">
                    <i class="fas fa-exclamation-triangle fa-3x text-danger mb-3"></i>
                    <p class="mb-0">Erro ao conectar com o servidor.</p>
                </div>
            `;
        }
    }

    // SALVAR NOTA
    async function salvarNotaAluno(alunoId) {
        const avaliacaoId = document.getElementById("avaliacaoSelect").value;
        const valor = document.getElementById(`nota_${alunoId}`).value;

        if (!avaliacaoId) {
            showAlert('Atenção', 'Selecione uma avaliação', 'warning');
            return;
        }

        if (!valor) {
            showAlert('Atenção', 'Digite a nota', 'warning');
            return;
        }

        const payload = {
            turmaId: turmaId,
            alunoId: alunoId,
            avaliacaoId: avaliacaoId,
            valor: parseFloat(valor)
        };

        const response = await fetch("/notas/lancar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const erro = await response.json();
            showAlert('Erro', erro.erro || "Erro ao salvar nota", 'error');
            return;
        }

        showToast('Nota salva com sucesso!', 'success');
    }

    // SALVAR FREQUÊNCIA
    async function salvarFrequencia() {
        const data = document.getElementById("dataFrequencia").value;

        if (!data) {
            showAlert('Atenção', 'Selecione uma data', 'warning');
            return;
        }

        const selects = document.querySelectorAll(".status-select");
        const lista = [];

        selects.forEach(select => {
            const alunoId = parseInt(select.getAttribute("data-aluno-id"));
            const status = select.value;
            lista.push({ alunoId: alunoId, status: status });
        });

        const payload = {
            turmaId: turmaId,
            data: data,
            frequencias: lista
        };

        const response = await fetch("/frequencia/salvar", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const erro = await response.json();
            showAlert('Erro', erro.erro || "Erro ao salvar frequência", 'error');
            return;
        }

        showToast('Frequência salva com sucesso!', 'success');
        atualizarResumoFrequencia();
    }

    // BUSCAR FREQUENCIA POR DATA
    async function buscarFrequenciaPorData() {
        const data = document.getElementById("dataFrequencia").value;
        if (!data) {
            showAlert('Atenção', 'Selecione uma data para buscar', 'warning');
            return;
        }
        
        const response = await fetch(`/frequencia/turma/${turmaId}/data/${data}`);
        if (!response.ok) {
            showAlert('Erro', 'Erro ao buscar frequência', 'error');
            return;
        }
        
        const lista = await response.json();
        const mapa = {};
        lista.forEach(f => { mapa[f.alunoId] = f.status; });
        
        document.querySelectorAll(".status-select").forEach(select => {
            const alunoId = select.getAttribute("data-aluno-id");
            if (mapa[alunoId]) select.value = mapa[alunoId];
        });
        
        atualizarResumoFrequencia();
        showToast('Frequência carregada para a data selecionada', 'success');
    }

    // ATUALIZA A BUSCA
    function atualizarResumoFrequencia() {
        const selects = document.querySelectorAll(".status-select");
        let presentes = 0, faltas = 0, justificadas = 0;
        selects.forEach(select => {
            if (select.value === 'PRESENTE') presentes++;
            else if (select.value === 'FALTA') faltas++;
            else if (select.value === 'JUSTIFICADA') justificadas++;
        });
        document.getElementById('presentesCount').innerText = presentes;
        document.getElementById('faltasCount').innerText = faltas;
        document.getElementById('justificadasCount').innerText = justificadas;
    }

    // ATUALIZA NOTAS AVALIAÇÕES
    document.getElementById("avaliacaoSelect").addEventListener("change", async function() {
        const avaliacaoId = this.value;
        if (!avaliacaoId) return;
        
        const response = await fetch(`/notas/turma/${turmaId}/avaliacao/${avaliacaoId}`);
        if (!response.ok) return;
        const notas = await response.json();
        notas.forEach(n => {
            const input = document.getElementById(`nota_${n.alunoId}`);
            if (input) input.value = n.valor;
        });
    });

    // FUNÇÕES UI
    function showSection(section) {
        document.querySelectorAll('[id^="section-"]').forEach(el => el.style.display = 'none');
        document.getElementById(`section-${section}`).style.display = 'block';
        document.querySelectorAll('.sidebar-menu-item').forEach(item => item.classList.remove('active'));
        event.target.closest('.sidebar-menu-item').classList.add('active');
        
        if (section === 'frequencia') {
            atualizarResumoFrequencia();
        }
    }

    function toggleSidebar() {
        document.getElementById('sidebar').classList.toggle('collapsed');
        document.getElementById('mainContent').classList.toggle('expanded');
    }

    function filterAlunosTurma() {
        const filter = document.getElementById('filterAlunosTurma').value.toLowerCase();
        const rows = document.querySelector('#section-alunos .table-custom tbody')?.rows;
        if (rows) {
            for (let row of rows) {
                const text = row.innerText.toLowerCase();
                row.style.display = text.includes(filter) ? '' : 'none';
            }
        }
    }

    function filterAlunosSelect() {
        const filter = document.getElementById('searchAlunoInput').value.toLowerCase();
        const options = document.querySelector('select[name="alunoId"]')?.options;
        if (options) {
            for (let opt of options) {
                if (opt.value === '') continue;
                opt.style.display = opt.text.toLowerCase().includes(filter) ? '' : 'none';
            }
        }
    }

    function abrirModalAvaliacao() {
        new bootstrap.Modal(document.getElementById('modalAvaliacao')).show();
    }

    async function salvarAvaliacao() {
        const nome = document.getElementById("nomeAvaliacao").value;
        const tipo = document.getElementById("tipoAvaliacao").value;
        const dataVencimento = document.getElementById("dataVencimento").value;
        const peso = document.getElementById("pesoAvaliacao").value;

        if (!nome || !dataVencimento) {
            showAlert('Atenção', 'Preencha todos os campos', 'warning');
            return;
        }

        const formData = new FormData();
        formData.append("nome", nome);
        formData.append("tipo", tipo);
        formData.append("dataVencimento", dataVencimento);
        formData.append("peso", peso || 0);
        formData.append("turmaId", turmaId);

        const response = await fetch("/avaliacoes/salvar", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            showAlert('Erro', 'Erro ao salvar avaliação', 'error');
            return;
        }

        showToast('Avaliação criada com sucesso!', 'success');
        setTimeout(() => location.reload(), 1500);
    }

    // FUNÇÕES DO TEMA
    let currentFontSize = 100;
    function setTheme(theme) {
        document.body.classList.remove('dark-theme', 'high-contrast');
        if (theme === 'dark') document.body.classList.add('dark-theme');
        if (theme === 'high-contrast') document.body.classList.add('high-contrast');
        localStorage.setItem('theme', theme);
        document.querySelectorAll('.theme-btn').forEach(btn => btn.classList.remove('active'));
        if (event && event.target) event.target.classList.add('active');
    }

    function changeFontSize(dir) {
        if (dir === -1) currentFontSize = Math.max(80, currentFontSize - 10);
        else if (dir === 1) currentFontSize = Math.min(130, currentFontSize + 10);
        else currentFontSize = 100;
        document.body.style.fontSize = currentFontSize + '%';
        localStorage.setItem('fontSize', currentFontSize);
    }

    // CARREGA PREFERÊNCIAS 
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) setTheme(savedTheme);
    const savedFont = localStorage.getItem('fontSize');
    if (savedFont) { currentFontSize = parseInt(savedFont); document.body.style.fontSize = currentFontSize + '%'; }
    
    // INICIA RESUMO
    document.addEventListener('DOMContentLoaded', function() {
        atualizarResumoFrequencia();
    });