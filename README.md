# Neon Pulse

Reprodutor de músicas locais com interface neon, controles de faixa e lista reorganizável.

## Arquivos
- `index.html`: entrada principal do aplicativo web.
- `neon_pulse_music.html`: arquivo original preservado.
- `manifest.json`: configuração de instalação PWA.
- `sw.js`: cache dos arquivos locais para abrir offline após a primeira visita.
- `icon.svg`: ícone do Neon Pulse, vetorial e centralizado.

## Publicar
Ative GitHub Pages nas configurações do repositório, usando a branch `main` e a pasta raiz.

## Observações
Os arquivos de áudio são selecionados localmente e não são enviados a servidores. Por segurança do navegador, a seleção de arquivos locais não fica disponível automaticamente após fechar e reabrir o app; selecione as músicas novamente. Esta versão é um PWA; ela ainda não é um APK Android nativo e não oferece serviço nativo de reprodução em segundo plano/tela bloqueada.

Desenvolvido por Genival Moreira.
