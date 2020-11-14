package com.example.app.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.executable.ValidateOnExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.app.dao.DocDAO;
import com.example.app.entidade.Doc;
import com.example.app.entidade.Pessoa;
import com.example.app.entidade.Usuario;
import com.example.app.service.PessoaService;
import com.example.app.service.UsuarioService;
import com.example.app.validators.OrdemMensagem;

@Controller
public class PessoaController {

	@Autowired
	private PessoaService pessoaService;
	@Autowired
	private DocDAO docDAO;
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private UsuarioService usuarioService;
	private final String URL_LOCAL = "http://localhost:8080";
	private final String URL_SITE = "https://agenda-spring.herokuapp.com";

	@GetMapping("/pessoa")
	public Pessoa pessoa(@RequestParam(value = "nome") String nome, @RequestParam(value = "tel") String telefone) {
		return new Pessoa(2123223L, nome, telefone);
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("pessoa", new Pessoa());
		return "index";
	}

	@GetMapping("/index")
	public String index(Model model) {
		model.addAttribute("pessoa", new Pessoa());
		return "index";
	}

	@GetMapping("/cadastro")
	public String cadastro(Model model, Authentication authentication) {
		Usuario usuario = usuarioService.buscarUsuarioPorEmail(authentication.getName());
		model.addAttribute("pessoa", new Pessoa());
		model.addAttribute("usuario", usuario);
		return "cadastro";
	}

	@GetMapping("/pessoas")
	public String pessoas(Model model, Authentication authentication) {
		model.addAttribute("pessoas", pessoaService.buscarTodosPorUsuario());
		return "pessoas";
	}

	@GetMapping("/pessoas/{page}")
	public String pessoasPaginacao(@PathVariable(value = "page") int page, Model model, Authentication auth) {
		if (page < 1) {
			page = 1;
		}
		// Antigo
		// Page<Pessoa> pessoas = pessoaService.findPaging(page);
		Page<Pessoa> pessoas = pessoaService.buscarPaginacaoPorUsuario(page);

		// atualizar notificacao
		Usuario usuario = usuarioService.buscarUsuarioPorEmail(auth.getName());
		usuarioService.salvarNotificacao(usuario);

		if (page > pessoas.getTotalPages()) {
			page = 1;
			pessoas = pessoaService.buscarPaginacaoPorUsuario(page);
		}
		model.addAttribute("pessoas", pessoas.getContent());
		model.addAttribute("numPaginas", pessoas.getTotalPages());
		model.addAttribute("page", page);

		return "pessoas";
	}

	@PostMapping("/salvar")
	public String salvar(@Validated(value = { OrdemMensagem.class }) Pessoa pessoa, BindingResult bindingResult,
			Model model, Authentication auth, @RequestParam("arquivos") MultipartFile[] files) throws IOException {
		if (bindingResult.hasErrors()) {
			return "cadastro";
		}
		Usuario usuario = usuarioService.buscarUsuarioPorEmail(auth.getName());
		model.addAttribute("msgCadastro", pessoa.getId() == null ? "Pessoa cadastrada!" : "Pessoa atualizada!");
		model.addAttribute("usuario", usuario);
		model.addAttribute("pessoa", new Pessoa());
		pessoaService.salvar(pessoa, files);
		return "cadastro";
	}

	@GetMapping(value = "/edit/{id}")
	public String edit(@PathVariable(name = "id") Long id, Model model) {
		Pessoa pessoa = pessoaService.buscarPorId(id);
		model.addAttribute("pessoa", pessoa);
		System.out.println(pessoa.toString());
		return "cadastro";
	}

	@GetMapping("/download/{id}")
	public ResponseEntity<Resource> downloadFile(@PathVariable(value = "id") long id) {
		Doc doc = docDAO.findById(id).get();

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(doc.getTipoArquivo()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + doc.getNomeArquivo() + "\"")
				.body(new ByteArrayResource(doc.getData()));
	}

	@GetMapping("/del/{id}")
	public String delete(@PathVariable(name = "id") Long id, RedirectAttributes ra) {
		pessoaService.remover(pessoaService.buscarPorId(id));
		ra.addFlashAttribute("msgDelete", "Pessoa removida!");
		return "redirect:/pessoas/1";
	}

	@GetMapping("/senha")
	public String senha() {
		String senha = new BCryptPasswordEncoder().encode("321");
		System.out.printf(senha);
		return "login";
	}

	@GetMapping("/sort/{tipo}")
	public String sort(@PathVariable(name = "tipo") String tipo, Model model, @RequestParam("pag") int pag,
			@RequestParam(name = "ordem", defaultValue = "asc") String ordem) {
		Page<Pessoa> sortPorTipo = pessoaService.sortPorTipo(tipo, ordem, pag);
		model.addAttribute("revDir", ordem.equals("asc") ? "desc" : "asc");
		model.addAttribute("pessoas", sortPorTipo.getContent());
		model.addAttribute("numPaginas", sortPorTipo.getTotalPages());
		model.addAttribute("page", pag);
		return "pessoas";
	}

	@GetMapping("/login")
	public String login() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "login";
		}
		return "redirect:/index";
	}

	@GetMapping("/del-doc")
	public String deleteDoc(@RequestParam(name = "id") Long id, RedirectAttributes ra) {
		docDAO.deleteById(id);
		ra.addFlashAttribute("msgDoc", "Arquivo removido!");
		return "redirect:/pessoas/1";
	}

	@Deprecated
	@GetMapping("/user")
	@ResponseBody
	public String criarUser() {
		try {
			Pessoa p = new Pessoa();
			p.setNome("Admin");
			p.setEmail("user@gmail.com");
			p.setAtivo(true);
			p.setDataCadastro(new Date());
			p.setSexo("M");
			pessoaService.salvar(p, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Usuário já criado!";
	}

	@Deprecated
	@GetMapping("/username")
	@ResponseBody
	public String getUser(Authentication authentication) {
		Usuario user = usuarioService.buscarUsuarioPorEmail(authentication.getName());
		Pessoa p = new Pessoa();
		p.setAtivo(true);
		p.setEmail("pessoaDoUsuario@usuario.com");
		p.setNome("Pessoa Usuario");
		user.getPessoas().add(p);
		pessoaService.salvar(p, null);
		return "salvo!";
	}

	@GetMapping("/usuario")
	public String usuario(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "usuario";
	}

	@PostMapping("/usuario")
	public String salvarUsuario(Model model, @Validated(value = { OrdemMensagem.class }) Usuario usuario,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors())
			return "usuario";

		usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));
		usuarioService.salvar(usuario);

		model.addAttribute("msgUsuario", "Usuario cadastrado!");
		return "usuario";
	}

	@GetMapping("/emailReset")
	public String emailReset() {
		return "emailReset";
	}

	@PostMapping("/emailReset")
	public String emailResetSend(HttpServletRequest request, Model model) throws MessagingException, IOException {
		String email = request.getParameter("email");
		Usuario usuario = usuarioService.buscarUsuarioPorEmail(email);
		if (usuario == null) {
			model.addAttribute("msgReset", "Usuário não encontrado!");
			return "emailReset";
		} else {
			String token = UUID.randomUUID().toString();
			usuario.setToken(token);
			usuarioService.salvar(usuario);
			String msg = "<html><body>";
			msg += "<h4>Prezado, Click no link abaixo para resetar sua senha!</h4>";
			msg += "<br />";
			msg += URL_SITE + "/verificaNovaSenha?token=" + usuario.getToken()
					+ " <br /> </body></html>";
			enviarEmail(email, msg);
			model.addAttribute("msgReset", "Email enviado com sucesso para: " + email + "!");
			return "emailReset";
		}
	}

	@GetMapping("/verificaNovaSenha")
	public String verificaNovaSenha(@RequestParam("token") String token, Model model) {
		Usuario usuario = usuarioService.buscarPorToken(token);
		if (usuario == null) {
			return "error";
		} else {
			model.addAttribute("usuario", usuario);
			return "novaSenha";
		}
	}

	@PostMapping("/novaSenha")
	public String redefinirNovaSenha(Model model, @Validated(value = { OrdemMensagem.class }) Usuario usuario,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			FieldError fieldError = bindingResult.getFieldError();
			if (!fieldError.getCode().equalsIgnoreCase("uniqueemail")) {
				model.addAttribute("usuario", usuario);
				return "novaSenha";
			}
		}
		usuario.setSenha(new BCryptPasswordEncoder().encode(usuario.getSenha()));
		usuarioService.salvar(usuario);
		model.addAttribute("msgNovaSenha", "Senha atualizada com sucesso!");
		model.addAttribute("usuario", new Usuario());
		return "novaSenha";
	}

	@GetMapping("/email")
	@ResponseBody
	public String email(HttpServletRequest request) throws MessagingException {
		String token = UUID.randomUUID().toString();
		String msg = "<html><body> Click no link abaixo para resetar sua senha";
		msg += "<br /> Esse Link tem um tempo de duração de 30 minutos: ";
		msg += "http://localhost:8080/verificaNovaSenha?token=" + token + " <br /> </body></html>";
		enviarEmail("viniciuseliasmoreira@hotmail.com", msg);
		return "email enviado com sucesso!";
	}

	public void enviarEmail(String emailTo, String msgEmail) throws MessagingException {
		JavaMailSenderImpl sender = emailConfig();
		MimeMessage mimeMessage = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
		helper.setTo(emailTo);
		helper.setSubject("CRUD-PESSOAS redefinição de nova senha");
		helper.setText(msgEmail, true);
		sender.send(mimeMessage);
	}

	public JavaMailSenderImpl emailConfig() {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setUsername("fake2fake2016@gmail.com");
		sender.setPassword("cmofasdotia");

		Properties mailProperties = new Properties();
		mailProperties.put("mail.smtp.host", "smtp.gmail.com");
		mailProperties.put("mail.smtp.auth", "true");
		mailProperties.put("mail.smtp.port", "587");
		mailProperties.put("mail.smtp.starttls.enable", "true");
		mailProperties.put("mail.smtp.socketFactory.port", "465");
		mailProperties.put("mail.smtp.socketFactory.fallback", "false");
		mailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		sender.setJavaMailProperties(mailProperties);
		return sender;
	}

	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public String erro() {
		return "erro";
	}

	@GetMapping("/password")
	@ResponseBody
	public String verificaSenha() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		boolean matches = encoder.matches("321", "$2a$10$PdwmKFfYpnPARb5aFr232eSq.r2xdwJ.NM9st0EUVJCxVn4zQbVDW");
		if (matches)
			return "true";
		else
			return "false";
	}

	@GetMapping(value = "/pesquisa")
	@ResponseBody
	public Map<String, String> pesquisa(@RequestParam(value = "texto") String texto, Authentication authentication) {
		Usuario usuario = usuarioService.buscarUsuarioPorEmail(authentication.getName());
		Page<Pessoa> pesquisa = pessoaService.buscarPorNome(texto, usuario.getId());
		String tbody = "<tbody>";
		String tbodyFile = "<tbody>";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String sexo = "";
		// primeira tabela
		for (Pessoa pessoa : pesquisa) {
			tbody += "<tr>";
			tbody += "<td>" + pessoa.getId() + "</td>";
			tbody += "<td style='max-width: 230px' class='text-break'>" + pessoa.getNome() + "</td>";
			tbody += "<td>" + pessoa.getEmail() + "</td>";
			tbody += "<td>" + (pessoa.getDataCadastro() != null ? sdf.format(pessoa.getDataCadastro()) : "N/A")
					+ "</td>";
			tbody += "<td>" + pessoa.getSexo() + "</td>";
			tbody += "<td>" + (pessoa.isAtivo() ? "Ativo" : "Desativado") + "</td>";
			tbody += "<td>" + pessoa.getCidade() + "</td>";
			tbody += "<td>" + pessoa.getEstado() + "</td>";
			tbody += "<td class='text-center'>";
			tbody += "<a class='btn btn-sm btn-primary mr-1' href='/edit/" + pessoa.getId()
					+ "'><i class='fas fa-pencil-alt'></i></a>";
			tbody += "<a onclick='return confirm(\"Deseja excluir?\")' class='btn btn-sm btn-danger' href='/del/"
					+ pessoa.getId() + "'><i class='fas fa-trash-alt'></i></a>";
			tbody += "</td>";
			tbody += "</tr>";
		}
		if (pesquisa.getContent().isEmpty()) {
			tbody += "<tr><td colspan='9' class='text-center h6'>Nenhum registro encontrado...</td></tr>";
			tbodyFile += "<tr><td colspan='9' class='text-center h6'>Nenhum registro encontrado...</td></tr>";
		}
		tbody += "</tbody>";
		// fim primeira tabela

		// segunda tabela
		for (Pessoa pessoa : pesquisa) {
			tbodyFile += "<tr>";
			tbodyFile += "<td>" + pessoa.getId() + "</td>";
			tbodyFile += "<td>" + pessoa.getNome() + "</td>";
			tbodyFile += "<td>" + (pessoa.getDataCadastro() != null ? sdf.format(pessoa.getDataCadastro()) : "N/A")
					+ "</td>";
			tbodyFile += "<td>" + (pessoa.isAtivo() ? "Ativo" : "Desativado") + "</td>";
			tbodyFile += "<td align='center' style='width:400px'><table class='w-100'>";
			for (Doc doc : pessoa.getDocs()) {
				tbodyFile += "<tr><td><div class='row' style='width: 400px;'>";
				// div btn Download
				tbodyFile += "<div class='col-sm-10'> <strong>Download:</strong> <br /> <a"
						+ "	style='word-wrap: break-word;'  href='/download/" + doc.getId() + "'>"
						+ doc.getNomeArquivo() + "	</a></div>";
				// div btn Delete Download
				tbodyFile += "<div class='col-sm-2 justify-content-end d-flex align-items-center'>"
						+ "	<a onclick='return confirm(\"Deseja excluir?\")' href='/del-doc?id=" + doc.getId()
						+ "' class='mr-2 btn btn-sm btn-danger'> <i class='fas fa-trash-alt'></i>" + "	</a>  </div>";
				tbodyFile += "</div></td></tr>";
			}
			if (pessoa.getDocs().isEmpty()) {
				tbodyFile += "<tr><td><p class='text-center' align='center'>Sem Arquivos</p></td></tr>";
			}
			tbodyFile += "</table></td></tr>";
		}
		tbodyFile += "</tbody>";
		// fim segunda tabela

		HashMap<String, String> json = new HashMap<String, String>();
		json.put("tbody", tbody);
		json.put("tbodyFile", tbodyFile);
		return json;
	}

}
