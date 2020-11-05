package com.example.app.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
	private UsuarioService usuarioService;

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
	public String cadastro(Model model) {
		model.addAttribute("pessoa", new Pessoa());
		return "cadastro";
	}

	@GetMapping("/pessoas")
	public String pessoas(Model model, Authentication authentication) {
		model.addAttribute("pessoas", pessoaService.buscarTodosPorUsuario());
		return "pessoas";
	}

	@GetMapping("/pessoas/{page}")
	public String pessoasPaginacao(@PathVariable(value = "page") int page, Model model) {
		// Antigo
		// Page<Pessoa> pessoas = pessoaService.findPaging(page);
		Page<Pessoa> pessoas = pessoaService.buscarPaginacaoPorUsuario(page);
		model.addAttribute("pessoas", pessoas.getContent());
		model.addAttribute("numPaginas", pessoas.getTotalPages());
		model.addAttribute("page", page);

		return "pessoas";
	}

	@PostMapping("/salvar")
	public String salvar(@Validated(value = {OrdemMensagem.class}) Pessoa pessoa, BindingResult bindingResult, Model model,
			@RequestParam("arquivos") MultipartFile[] files) throws IOException {
		if (bindingResult.hasErrors()) {
			return "cadastro";
		}
		model.addAttribute("msgCadastro", pessoa.getId() == null ? "Pessoa cadastrada!" : "Pessoa atualizada!");
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
	public String salvarUsuario(Model model, @Validated(value = {OrdemMensagem.class}) Usuario usuario, BindingResult bindingResult) {
		if (bindingResult.hasErrors())
			return "usuario";
		usuarioService.salvar(usuario);
		model.addAttribute("msgUsuario", "Usuario cadastrado!");
		return "usuario";
	}

	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public String erro() {
		return "erro";
	}

	@GetMapping(value = "/pesquisa")
	@ResponseBody
	public Map<String, String> pesquisa(@RequestParam(value = "texto") String texto,
			Authentication authentication) {
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
			tbody += "<td>" + (pessoa.getDataCadastro() != null ? sdf.format(pessoa.getDataCadastro()) : "N/A") + "</td>";
			if (pessoa.getSexo().equals("M")) {
				sexo = "Masculino";
			} else if (pessoa.getSexo().equals("F")) {
				sexo = "Feminino";
			} else if (pessoa.getSexo().equals("N")) {
				sexo = "Neutro";
			}
			tbody += "<td>" + sexo + "</td>";
			tbody += "<td>" + (pessoa.isAtivo() ? "Ativo" : "Desativado") + "</td>";
			tbody += "<td>" + pessoa.getCidade() + "</td>";
			tbody += "<td>" + pessoa.getEstado() + "</td>";
			tbody += "<td class='text-center'>";
			tbody += "<a class='btn btn-sm btn-primary mr-1' href='/edit/" + pessoa.getId()
					+ "'><i class='fas fa-pencil-alt'></i></a>";
			tbody += "<a onclick='return confirm(\"Deseja excluir?\")' class='btn btn-sm btn-danger' href='/del/" + pessoa.getId()
					+ "'><i class='fas fa-trash-alt'></i></a>";
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
			tbodyFile += "<td>" + (pessoa.getDataCadastro() != null ? sdf.format(pessoa.getDataCadastro()) : "N/A") + "</td>";
			tbodyFile += "<td>" + (pessoa.isAtivo() ? "Ativo" : "Desativado") + "</td>";
			tbodyFile += "<td align='center' style='width:350px'><table class='w-100'>";
			for (Doc doc : pessoa.getDocs()) {
				tbodyFile += "<tr><td><div class='row'>";
				//div btn Download
				tbodyFile += "<div class='col-sm-10'> <strong>Download:</strong> <br /> <a"
						+ "	style='word-wrap: break-word;'  href='/download/"
						+ doc.getId() + "'>" + doc.getNomeArquivo() + "	</a></div>";
				//div btn Delete Download
				tbodyFile += "<div class='col-sm-2 justify-content-end d-flex align-items-center'>"
						+ "	<a onclick='return confirm(\"Deseja excluir?\")' href='/del-doc?id=" + doc.getId()
						+ "' class='mr-2 btn btn-sm btn-danger'> <i class='fas fa-trash-alt'></i>"
						+ "	</a>  </div>";
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
