// EFEITO NO LOADING E VALIDAÇÃO
const loginForm = document.getElementById('loginForm');
const loginBtn = document.getElementById('loginBtn');
const emailInput = document.getElementById('email');
const senhaInput = document.getElementById('senha');
const validationError = document.getElementById('validationError');
const errorMessage = document.getElementById('errorMessage');

// MOSTRA ERRO SE EXISTIR 
if (errorMessage && errorMessage.querySelector('span').textContent.trim() !== '') {
    errorMessage.style.display = 'block';
    setTimeout(() => {
        errorMessage.style.opacity = '0';
        setTimeout(() => {
            errorMessage.style.display = 'none';
            errorMessage.style.opacity = '1';
        }, 500);
    }, 5000);
}

// VALIDA EMAIL
function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

// EFEITO FOCUS NOS INPUTS
const inputs = document.querySelectorAll('.input-group input');
inputs.forEach(input => {
    input.addEventListener('focus', function() {
        this.parentElement.style.transform = 'translateX(5px)';
    });
    
    input.addEventListener('blur', function() {
        this.parentElement.style.transform = 'translateX(0)';
    });
});

// SUBMIT FORMULÁRIO
loginForm.addEventListener('submit', function(e) {
    let hasError = false;
    
    // VALIDA EMAIL
    if (!validateEmail(emailInput.value)) {
        validationError.querySelector('span').textContent = 'Por favor, insira um e-mail válido.';
        validationError.style.display = 'block';
        emailInput.style.borderColor = '#f44336';
        hasError = true;
        setTimeout(() => {
            validationError.style.display = 'none';
        }, 3000);
        e.preventDefault();
        return;
    } else {
        emailInput.style.borderColor = '#e0e0e0';
    }
    
    // VALIDA SENHA
    if (senhaInput.value.length < 6) {
        validationError.querySelector('span').textContent = 'A senha deve ter pelo menos 6 caracteres.';
        validationError.style.display = 'block';
        senhaInput.style.borderColor = '#f44336';
        hasError = true;
        setTimeout(() => {
            validationError.style.display = 'none';
        }, 3000);
        e.preventDefault();
        return;
    } else {
        senhaInput.style.borderColor = '#e0e0e0';
    }
    
    if (!hasError) {
        // ADICIONA EFEITO NO LOADING
        loginBtn.classList.add('loading');
        loginBtn.innerHTML = '';
        
        // LEMBRAR-ME (STORAGE)
        const rememberMe = document.getElementById('rememberMe');
        if (rememberMe.checked) {
            localStorage.setItem('rememberedEmail', emailInput.value);
        } else {
            localStorage.removeItem('rememberedEmail');
        }
        
        setTimeout(() => {
            // REMOVE O LOADING SE DEMORAR MAIS QUE 3s
            loginBtn.classList.remove('loading');
            loginBtn.innerHTML = '<i class="fas fa-sign-in-alt" style="margin-right: 8px;"></i> Entrar';
        }, 3000);
    }
});

// CARREGA EMAIL SALVO STORAGE
const rememberedEmail = localStorage.getItem('rememberedEmail');
if (rememberedEmail) {
    emailInput.value = rememberedEmail;
    document.getElementById('rememberMe').checked = true;
}

// EFEITO AO CARREGAR PÁGINA
window.addEventListener('load', function() {
    const elements = document.querySelectorAll('.banner-overlay > *, .login-area > *');
    elements.forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        setTimeout(() => {
            el.style.transition = 'all 0.5s ease';
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        }, index * 100);
    });
});

// EVITA SUBMIT DUPLICADO
let isSubmitting = false;
loginForm.addEventListener('submit', function(e) {
    if (isSubmitting) {
        e.preventDefault();
        return;
    }
    isSubmitting = true;
    setTimeout(() => {
        isSubmitting = false;
    }, 3000);
});

// EFEITO BOTÕES DAS REDES SOCIAIS
const socialIcons = document.querySelectorAll('.social-icon');
socialIcons.forEach(icon => {
    icon.addEventListener('click', function(e) {
        e.preventDefault();
        
        this.style.transform = 'scale(0.95)';
        setTimeout(() => {
            this.style.transform = '';
        }, 200);
        
        // MOSTRA MENSAGEM DE IMPLEMENTAÇÃO
        const platform = this.classList.contains('facebook') ? 'Facebook' :
                        this.classList.contains('twitter') ? 'Twitter' :
                        this.classList.contains('instagram') ? 'Instagram' : 'LinkedIn';
        validationError.querySelector('span').textContent = `Login com ${platform} será implementado em breve.`;
        validationError.style.display = 'block';
        setTimeout(() => {
            validationError.style.display = 'none';
        }, 3000);
    });
});

// EFEITO NO BANNER
if (window.innerWidth > 968) {
    const banner = document.querySelector('.banner-side');
    window.addEventListener('mousemove', function(e) {
        const mouseX = e.clientX / window.innerWidth;
        const mouseY = e.clientY / window.innerHeight;
        const bannerImage = document.querySelector('.banner-image');
        if (bannerImage) {
            bannerImage.style.transform = `scale(1.05) translate(${mouseX * 10}px, ${mouseY * 10}px)`;
        }
    });
}