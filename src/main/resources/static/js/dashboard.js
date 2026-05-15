const turmaId = document.body.dataset.turmaId;

let alunosData = [];
let performanceChart;
let situacaoChart;
let currentFontSize = 100;

// CARREGAR DADOS DA TURMA
async function loadData() {
    try {
        const response = await fetch(`/dashboard/${turmaId}/dados`);

        if (!response.ok) {
            let erro;

            try {
               const data = await response.json();
               erro = data?.erro || "Erro ao carregar dados";
            } catch {
                 erro = await response.text();
            }

            showError(erro);
            return;
        }

        const data = await response.json();

        alunosData = data.alunos;

        document.getElementById('totalAlunos').innerText = data.totalAlunos;
        document.getElementById('mediaGeral').innerText = data.mediaGeral.toFixed(2);
        document.getElementById('aprovados').innerText = data.aprovados;
        document.getElementById('reprovados').innerText = data.reprovados;

        updateTable();
        initCharts();

    } catch (error) {
        showError("Erro inesperado ao carregar dados");
    }
}

// TABELA
function updateTable() {
    const tbody = document.getElementById('tableBody');
    tbody.innerHTML = '';

    if (!alunosData || alunosData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Nenhum aluno cadastrado</td></tr>';
        return;
    }

    alunosData.forEach((aluno, index) => {
        const row = tbody.insertRow();

        row.insertCell(0).innerText = index + 1;
        row.insertCell(1).innerText = aluno.ra || 'N/A';
        row.insertCell(2).innerText = aluno.nome;
        row.insertCell(3).innerText = aluno.email;
        row.insertCell(4).innerText = aluno.mediaNotas.toFixed(2);

        row.insertCell(5).innerHTML =
            `<span class="badge-${aluno.situacao === 'Aprovado' ? 'aprovado' : 'reprovado'}">
                ${aluno.situacao}
            </span>`;

        row.insertCell(6).innerHTML =
            `<button class="btn btn-sm btn-info" onclick="verDetalhes(${aluno.id})">
                <i class="fas fa-eye"></i> Ver
            </button>`;
    });
}


// FILTRO TABELA
function filterTable() {
    const filter = document.getElementById('searchInput').value.toUpperCase();
    const rows = document.getElementById('tableBody').getElementsByTagName('tr');

    for (let i = 0; i < rows.length; i++) {
        const textoLinha = rows[i].innerText.toUpperCase();
        rows[i].style.display = textoLinha.includes(filter) ? '' : 'none';
    }
}


// DETALHES DO ALUNO
async function verDetalhes(id) {
    try {
        const response = await fetch(`/dashboard/${turmaId}/aluno/${id}`);
        const aluno = await response.json();

        if (!aluno) return;

        let notasHtml = '';

        if (aluno.notas && aluno.notas.length > 0) {
            notasHtml = '<h6 class="mt-3">📄 Boletim Parcial:</h6><ul class="list-group">';

            aluno.notas.forEach(nota => {
                let badgeClass = '';

                if (nota.valor < 5) badgeClass = 'bg-danger';
                else if (nota.valor === 5) badgeClass = 'bg-warning text-dark';
                else badgeClass = 'bg-success';

                notasHtml += `
                    <li class="list-group-item">
                        <div class="d-flex justify-content-between align-items-center">
                            <span><strong>TIPO DE AVALIAÇÃO:</strong> ${nota.avaliacao}</span>
                            <span>
                                <strong>NOTA:</strong>
                                <span class="badge ${badgeClass}">${nota.valor}</span>
                            </span>
                        </div>
                    </li>
                `;
            });

            notasHtml += '</ul>';
        } else {
            notasHtml = '<p class="mt-3 text-muted">Nenhuma nota cadastrada</p>';
        }

        document.getElementById('modalBody').innerHTML = `
            <div class="row">
                <div class="col"><strong>RA:</strong> ${aluno.ra}</div>
                <div class="col"><strong>Nome:</strong> ${aluno.nome}</div>
            </div>

            <br>

            <p><strong>Email:</strong> ${aluno.email}</p>

            <div class="row">
                <div class="col">
                    <strong>Média Final:</strong> ${aluno.mediaNotas.toFixed(2)}
                </div>
                <div class="col">
                    <strong>Situação:</strong>
                    <span class="badge-${aluno.situacao === 'Aprovado' ? 'aprovado' : 'reprovado'}">
                        ${aluno.situacao}
                    </span>
                </div>
            </div>

            ${notasHtml}
        `;

        new bootstrap.Modal(document.getElementById('detalhesModal')).show();

    } catch (error) {
        console.error(error);
        alert('Erro ao carregar detalhes');
    }
}


// MOSTRAR ERRO NA TELA
function showError(mensagem) {

    if (!mensagem || mensagem === "undefined") {
        mensagem = "Erro inesperado no sistema";
    }

    document.getElementById('dashboardContent').innerHTML = `
        <div class="alert alert-danger text-center">
            <h4><i class="fas fa-exclamation-triangle"></i> Erro</h4>
            <p>${mensagem}</p>
            <a href="/turmas" class="btn btn-primary mt-2">
                Voltar para turmas
            </a>
        </div>
    `;
}


// ERRO VINDO DO CONTROLLER
window.addEventListener("DOMContentLoaded", () => {
    const erroDiv = document.getElementById("erroServidor");

    if (erroDiv) {
        const mensagem = erroDiv.innerText.trim();

        if (mensagem !== "") {
            showError(mensagem);
        }
    }
 });


// GRÁFICOS
function initCharts() {
    const nomes = alunosData.map(a => a.nome);
    const medias = alunosData.map(a => a.mediaNotas);

    const cores = medias.map(m =>
        m < 5 ? '#dc3545' : (m === 5 ? '#ffc107' : '#198754')
    );

    const ctx = document.getElementById('performanceChart').getContext('2d');

    if (performanceChart) performanceChart.destroy();

    performanceChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: nomes,
            datasets: [{
                label: 'Média',
                data: medias,
                backgroundColor: cores,
                borderRadius: 5
            }]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true, max: 10 } }
        }
    });

    const aprovados = alunosData.filter(a => a.situacao === 'Aprovado').length;
    const reprovados = alunosData.filter(a => a.situacao === 'Reprovado').length;

    if (situacaoChart) situacaoChart.destroy();

    situacaoChart = new ApexCharts(
        document.querySelector("#situacaoChart"),
        {
            series: [aprovados, reprovados],
            chart: { type: 'donut', height: 350 },
            labels: ['Aprovados', 'Reprovados'],
            colors: ['#28a745', '#dc3545']
        }
    );

    situacaoChart.render();
}

// SIDEBAR
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('collapsed');
    document.getElementById('mainContent').classList.toggle('expanded');
}

// TEMA
function setTheme(theme, e) {
    document.body.classList.remove('dark-theme', 'high-contrast');

    if (theme === 'dark') document.body.classList.add('dark-theme');
    if (theme === 'high-contrast') document.body.classList.add('high-contrast');

    localStorage.setItem('theme', theme);

    document.querySelectorAll('.theme-btn')
        .forEach(b => b.classList.remove('active'));

    if (e?.target) e.target.closest('.theme-btn').classList.add('active');
}

// FONT SIZE
function changeFontSize(dir) {
    if (dir === -1) currentFontSize = Math.max(80, currentFontSize - 10);
    else if (dir === 1) currentFontSize = Math.min(130, currentFontSize + 10);
    else currentFontSize = 100;

    document.body.style.fontSize = currentFontSize + '%';
    localStorage.setItem('fontSize', currentFontSize);
}

// EXPORT / PRINT
function exportToExcel() {
    const table = document.getElementById('alunosTable');
    const url = 'data:application/vnd.ms-excel,' + encodeURIComponent(table.outerHTML);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'alunos.xls';
    a.click();
}

function printTable() {
    const content = document.getElementById('alunosTable').outerHTML;
    document.body.innerHTML = `<h2>Relatório</h2>${content}`;
    window.print();
    location.reload();
}

// INIT
function loadPreferences() {
    const theme = localStorage.getItem('theme');
    if (theme) setTheme(theme);

    const font = localStorage.getItem('fontSize');
    if (font) {
        currentFontSize = parseInt(font);
        document.body.style.fontSize = font + '%';
    }
}

loadPreferences();
loadData();