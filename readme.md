# 🎵 NEON PULSE MUSIC

> **Reprodutor de áudio offline moderno com estética Cyberpunk & Synthwave Neon.**  
> Criado e desenvolvido por **Genival**.

![GitHub license](https://img.shields.io/badge/license-MIT-cyan?style=flat-square)
![PWA Ready](https://img.shields.io/badge/PWA-Ready-ff007f?style=flat-square)
![Offline First](https://img.shields.io/badge/Offline-100%25-00f0ff?style=flat-square)

---

## ✨ Características Principais

- ⚡ **100% Offline & Privado**: Seus arquivos de áudio não são enviados para nenhum servidor externo; a leitura é feita localmente no seu navegador via Blob URLs de alta velocidade.
- 🎨 **Design Neon Synthwave**: Visual imersivo com disco de vinil giratório, iluminação ciano elétrico (`#00f0ff`) e magenta (`#ff007f`), e arte com pulso neon sutil.
- 📱 **Progressive Web App (PWA)**: Pode ser instalado como aplicativo nativo no Android, iOS, Windows, macOS e Linux diretamente pelo navegador.
- 🔀 **Controle de Fila & Reordenação**: Arraste e solte (Drag & Drop) faixas para mudar a ordem das músicas com persistência imediata.
- 🎛️ **Controles de Reprodução Completos**:
  - Play, Pause, Próxima e Anterior.
  - Modo Aleatório (*Shuffle*) e Repetição contínua (*Repeat*).
  - Barra de progresso com busca em tempo real (*Scrubber*).
- 🧹 **Interface Limpa & Direta**: Sem elementos supérfluos, focada exclusivamente na melhor experiência musical.

---

## 📁 Estrutura de Arquivos do Repositório

```text
├── index.html        # Página principal com todo o layout, estilos e lógica de reprodução
├── manifest.json     # Metadados de instalação PWA (nome, cores e ícones)
├── sw.js             # Service Worker para funcionamento 100% offline
├── icon-192.png      # Ícone do aplicativo em resolução 192x192
├── icon-512.png      # Ícone do aplicativo em resolução 512x512
└── README.md         # Documentação e instruções de implantação
```

---

## 🚀 Como Publicar no GitHub Pages

1. Crie um novo repositório no seu GitHub (exemplo: `neon-pulse-music`) como **Público**.
2. Faça o upload dos arquivos:
   - `index.html`
   - `manifest.json`
   - `sw.js`
   - `icon-192.png` e `icon-512.png`
   - `README.md`
3. Vá em **Settings** > **Pages**.
4. Em **Branch**, selecione `main` (ou `master`) e salve.
5. Em menos de 2 minutos, o app estará disponível em:  
   `https://seu-usuario.github.io/neon-pulse-music/`

---

## 📲 Como Instalar no Smartphone (Android / iOS)

### No Android (Google Chrome):
1. Abra o link gerado pelo GitHub Pages no Chrome.
2. Toque no banner inferior **"Adicionar à tela inicial"** ou toque nos três pontinhos no canto superior direito e selecione **"Instalar aplicativo"**.
3. O ícone do **NEON PULSE** aparecerá na gaveta de aplicativos do seu celular e abrirá em tela cheia sem barras do navegador.

### No iPhone / iPad (Safari):
1. Abra o link no Safari.
2. Toque no botão de **Compartilhar** (ícone do quadrado com a seta para cima).
3. Role para baixo e selecione **"Adicionar à Tela de Início"**.

---

## 👨‍💻 Créditos

Desenvolvido com dedicação por **Genival**.  
Todos os direitos reservados. Sinta-se livre para clonar e customizar!
