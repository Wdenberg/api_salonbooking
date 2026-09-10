# ADR 0004 — JWT como Mecanismo de Autenticação

## Status

Aceito

## Contexto

O sistema precisa autenticar quatro tipos de usuário (`PLATFORM_ADMIN`, `OWNER`,
`EMPLOYEE`, `CUSTOMER`) em uma API REST que deve, desde o início, ser compatível com
escalabilidade horizontal — múltiplas instâncias da aplicação atrás de um load
balancer, sem estado compartilhado obrigatório entre elas (Seção 110 do escopo
original).

Autenticação baseada em sessão de servidor (session cookies + armazenamento de sessão
no lado do servidor) exigiria uma das seguintes soluções para funcionar corretamente
com múltiplas instâncias:

- Sticky sessions no load balancer (acopla o cliente a uma instância específica,
  complicando deploys e rolling updates).
- Armazenamento de sessão compartilhado (ex.: Redis) — uma dependência de
  infraestrutura adicional e um ponto único de falha para autenticação, antes mesmo
  de haver necessidade comprovada de cache distribuído no restante do sistema (Seção 7
  já prevê cache local como ponto de partida).

Além disso, o sistema precisa resolver autorização por *ownership* (não apenas por
role) em praticamente toda operação sensível: um `EMPLOYEE` só deve poder agir dentro
do `Business` ao qual pertence. Esse contexto de tenant precisa estar disponível de
forma eficiente em toda requisição autenticada, sem uma consulta ao banco a cada
chamada apenas para descobrir "a qual negócio este usuário pertence".

## Decisão

Adotar **autenticação stateless via JWT** (JSON Web Tokens), assinados com HMAC-SHA256
(chave simétrica de no mínimo 256 bits, validada na inicialização da aplicação).

O token carrega apenas as claims estritamente necessárias para autorização:

```json
{
  "sub": "<userId>",
  "roles": ["OWNER"],
  "businessId": "<businessId ou omitido>",
  "email": "...",
  "iat": ...,
  "exp": ...
}
```

- `businessId` é resolvido automaticamente no momento da emissão do token, **apenas
  para usuários com role `EMPLOYEE`** ativos, via o port `BusinessContextResolver`
  (implementado no módulo `employee`, mantendo `identity` desacoplado de módulos que
  dependem dele). Isso elimina a necessidade de uma consulta ao banco em toda
  requisição só para resolver o tenant de um funcionário.
- Nenhuma informação sensível (senha, hash de senha, tokens de terceiros) é incluída
  no payload — o JWT não é criptografado, apenas assinado, e seu conteúdo é
  publicamente legível por quem o possui.
- Um filtro (`JwtAuthenticationFilter`) reconstrói o `AuthenticatedUser` diretamente a
  partir das claims do token a cada requisição, sem round-trip ao banco de dados.

Autorização por *ownership* nunca depende apenas da presença de uma role ou do
`businessId` do token isoladamente — toda operação sensível compara explicitamente o
ID do recurso contra o usuário autenticado (`business.ownerId ==
authenticatedUser.userId`, `appointment.customerId == authenticatedUser.userId`),
implementado diretamente nos use cases, nunca confiando em um `businessId` enviado
pelo cliente no corpo da requisição (Seção 124 do escopo original).

## Alternativas Consideradas

### Sessões de servidor com armazenamento compartilhado (Redis)

Rejeitada para o MVP. Introduziria uma dependência de infraestrutura adicional antes
de qualquer necessidade comprovada de cache distribuído em outras partes do sistema —
seria adiantar uma decisão de infraestrutura (Redis) que a Seção 7 do escopo original
já trata como evolução futura opcional, não como requisito inicial.

### OAuth2 / OpenID Connect com um Identity Provider externo

Considerada como evolução futura razoável (especialmente para SSO corporativo ou login
social de clientes), mas rejeitada para o MVP por adicionar complexidade de integração
e uma dependência externa sem necessidade comprovada — o sistema já implementa seu
próprio cadastro/login (Seção 89), e delegar isso a um provedor externo desde o início
seria antecipar um requisito não solicitado.

### JWT com refresh tokens desde o MVP

Avaliada e adiada deliberadamente (Seção 50 do escopo original já sinaliza isso como
evolução futura). O MVP usa apenas access tokens de curta duração configurável — a
ausência de refresh tokens é uma lacuna conhecida, não um esquecimento, documentada no
README como próximo passo natural quando a experiência de sessão longa se tornar um
requisito de produto.

## Consequências

**Positivas**

- Nenhum estado de autenticação compartilhado entre instâncias da aplicação — qualquer
  instância pode validar qualquer token de forma independente, apenas verificando a
  assinatura, o que é compatível por design com múltiplas réplicas atrás de um load
  balancer sem sticky sessions.
- Nenhuma consulta ao banco de dados é necessária para autenticar uma requisição — o
  custo de autenticação é constante (verificação criptográfica da assinatura), não
  proporcional ao tráfego do banco.
- `businessId` pré-resolvido no token elimina uma consulta repetida em toda operação
  de um `EMPLOYEE`, sem comprometer a segurança, já que a autorização por ownership
  continua sendo verificada explicitamente contra o recurso, não apenas confiando na
  claim do token isoladamente.

**Negativas / trade-offs aceitos**

- **Revogação de token antes da expiração não é possível** com JWT stateless puro: uma
  vez emitido, um token é válido até expirar, mesmo que o usuário seja desativado ou
  bloqueado no meio desse intervalo. Mitigado parcialmente pela expiração configurável
  (`JWT_EXPIRATION_SECONDS`, padrão 1 hora) e pela verificação de `UserStatus.ACTIVE`
  no momento do login — mas não impede o uso de um token já emitido para um usuário
  bloqueado *depois* da emissão, até a expiração natural. Endereçar isso
  completamente exigiria uma lista de revogação (blacklist) compartilhada — uma
  reintrodução de estado compartilhado que o design atual evita deliberadamente;
  documentado como limitação conhecida.
- Se o `businessId` de um `EMPLOYEE` mudar (ex.: transferido para outro negócio — não
  suportado atualmente, mas hipoteticamente), o token antigo continuaria carregando o
  `businessId` anterior até expirar e ser reemitido via novo login.
- Payload do token é publicamente legível por quem o possui (apenas assinado, não
  criptografado) — reforça a necessidade de nunca incluir dados sensíveis nas claims,
  uma disciplina que precisa ser mantida conscientemente à medida que novas claims
  forem eventualmente adicionadas.
