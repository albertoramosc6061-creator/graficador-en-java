package mathgraph.core.parser;

import java.util.*;

public class CalculationPipeline {
    private final List<FlowchartNode> nodes;
    private final Map<String, FlowchartNode> nodeMap;

    public CalculationPipeline() {
        this.nodes = new ArrayList<>();
        this.nodeMap = new LinkedHashMap<>();
    }

    public static CalculationPipeline generate(String expression, double xMin, double xMax, int samples, double t, CompiledExpression compiled, int rootCount, int extremaCount, double integralVal) {
        CalculationPipeline pipeline = new CalculationPipeline();

        // 1. Input Node
        FlowchartNode nodeInput = new FlowchartNode("INPUT", "1. Expresión & Dominio", "INPUT", "Parámetros y función de entrada");
        nodeInput.addDetail("f(x, t) = " + expression);
        nodeInput.addDetail(String.format("Dominio: [%.2f, %.2f]", xMin, xMax));
        nodeInput.addDetail("Resolución: " + samples + " muestras");
        nodeInput.addDetail(String.format("Parámetro t = %.3f", t));
        nodeInput.addOutput("LEXER");
        pipeline.addNode(nodeInput);

        // 2. Lexer Tokenizer
        FlowchartNode nodeLexer = new FlowchartNode("LEXER", "2. Análisis Léxico (Lexer)", "PARSER", "Tokenización de la cadena");
        try {
            MathLexer lexer = new MathLexer(expression);
            List<Token> tokens = lexer.tokenize();
            nodeLexer.addDetail("Total de tokens: " + tokens.size());
            StringBuilder sampleTokens = new StringBuilder("Tokens: ");
            for (int i = 0; i < Math.min(6, tokens.size()); i++) {
                if (i > 0) sampleTokens.append(", ");
                sampleTokens.append(tokens.get(i).getText());
            }
            if (tokens.size() > 6) sampleTokens.append("...");
            nodeLexer.addDetail(sampleTokens.toString());
            nodeLexer.addDetail("Inserción de mult. implícita: OK");
        } catch (Exception e) {
            nodeLexer.addDetail("Error de tokenización: " + e.getMessage());
        }
        nodeLexer.addOutput("AST");
        pipeline.addNode(nodeLexer);

        // 3. Parser AST Node
        FlowchartNode nodeAst = new FlowchartNode("AST", "3. Árbol Sintáctico (AST)", "PARSER", "Estructura jerárquica de operadores");
        if (compiled != null && compiled.getRootNode() != null) {
            nodeAst.addDetail("Tipo Raíz: " + compiled.getRootNode().getClass().getSimpleName());
            nodeAst.addDetail("Forma Infix: " + compiled.getRootNode().toInfix());
            nodeAst.addDetail("Precedencia: Operadores & Funciones");
        } else {
            nodeAst.addDetail("Estado: Árbol no compilado");
        }
        nodeAst.addOutput("DISCRETIZATION");
        pipeline.addNode(nodeAst);

        // 4. Discretization Sampler
        double dx = (xMax - xMin) / (double) Math.max(1, samples);
        FlowchartNode nodeSampler = new FlowchartNode("DISCRETIZATION", "4. Muestreo del Dominio", "SAMPLER", "Generación de malla espacial");
        nodeSampler.addDetail(String.format("Paso espacial Δx = %.5f", dx));
        nodeSampler.addDetail("Intervalo: [" + xMin + " → " + xMax + "]");
        nodeSampler.addDetail("Generador de pasos: x_i = x0 + i*Δx");
        nodeSampler.addOutput("EVAL");
        pipeline.addNode(nodeSampler);

        // 5. Numerical Evaluator
        FlowchartNode nodeEval = new FlowchartNode("EVAL", "5. Evaluación Numérica", "EVALUATOR", "Cálculo punto a punto y = f(x_i, t)");
        nodeEval.addDetail("Evaluación recursiva del AST");
        nodeEval.addDetail("Manejo de asíntotas y NaNs: Activo");
        nodeEval.addDetail("Total de evaluaciones: " + samples + " puntos");
        nodeEval.addOutput("CALCULUS");
        nodeEval.addOutput("DATABASE");
        pipeline.addNode(nodeEval);

        // 6. Calculus & Extrema Node
        FlowchartNode nodeCalculus = new FlowchartNode("CALCULUS", "6. Motor de Cálculo", "CALCULUS", "Derivadas, Integrales y Raíces");
        nodeCalculus.addDetail(String.format("Raíces (Ceros) detectados: %d", rootCount));
        nodeCalculus.addDetail(String.format("Puntos Críticos / Extremos: %d", extremaCount));
        nodeCalculus.addDetail(String.format("Integral ∫ f(x)dx ≈ %.4f", integralVal));
        nodeCalculus.addDetail("Algoritmo raíces: Bisección + Newton");
        nodeCalculus.addOutput("RENDER");
        pipeline.addNode(nodeCalculus);

        // 7. Relational Database Node
        FlowchartNode nodeDb = new FlowchartNode("DATABASE", "7. Base de Datos en Memoria", "DATABASE", "Almacenamiento tabular y consultas SQL");
        nodeDb.addDetail("Campos: [id, x, y, f', f'', ∫f, tipo]");
        nodeDb.addDetail("Indexación de puntos clave y filtros");
        nodeDb.addDetail("Exportación: SQL, CSV, JSON disponible");
        pipeline.addNode(nodeDb);

        // 8. Transform & Screen Projection
        FlowchartNode nodeTransform = new FlowchartNode("TRANSFORM", "8. Transformación Cartesiana", "RENDER", "Mapeo ℝ² → Píxeles de Pantalla");
        nodeTransform.addDetail("Sx = (x - xMin) * (W / (xMax - xMin))");
        nodeTransform.addDetail("Sy = (yMax - y) * (H / (yMax - yMin))");
        nodeTransform.addDetail("Soporte Zoom / Pan con mouse");
        nodeTransform.addOutput("RENDER");
        pipeline.addNode(nodeTransform);

        // Connect Evaluation to Transform
        nodeEval.addOutput("TRANSFORM");

        // 9. Canvas Rendering
        FlowchartNode nodeRender = new FlowchartNode("RENDER", "9. Renderizado Gráfico 2D", "RENDER", "Dibujo en lienzo con Antialiasing");
        nodeRender.addDetail("1. Cuadrícula y Ejes coordenados");
        nodeRender.addDetail("2. Rectángulos / Cajas de Riemann");
        nodeRender.addDetail("3. Curva continua y Relleno degradado");
        nodeRender.addDetail("4. Marcadores de Raíces y Extremos");
        nodeRender.addDetail("5. Rastreador de Cursor y Tangente");
        pipeline.addNode(nodeRender);

        // Auto layout nodes in a 2D workflow structure
        pipeline.layoutFlowchart();

        return pipeline;
    }

    private void addNode(FlowchartNode node) {
        nodes.add(node);
        nodeMap.put(node.getId(), node);
    }

    public List<FlowchartNode> getNodes() {
        return nodes;
    }

    public FlowchartNode getNode(String id) {
        return nodeMap.get(id);
    }

    private void layoutFlowchart() {
        // Multi-column beautiful flowchart layout
        // Col 0: Input -> Lexer -> AST
        // Col 1: Discretization -> Evaluation -> Calculus -> Database
        // Col 2: Transform -> Render

        int colWidth = 260;
        int rowHeight = 150;
        int startX = 40;
        int startY = 40;

        // Column 0 (Parsing Pipeline)
        setPos("INPUT", startX, startY);
        setPos("LEXER", startX, startY + rowHeight);
        setPos("AST", startX, startY + rowHeight * 2);

        // Column 1 (Computation Pipeline)
        setPos("DISCRETIZATION", startX + colWidth, startY);
        setPos("EVAL", startX + colWidth, startY + rowHeight);
        setPos("CALCULUS", startX + colWidth, startY + rowHeight * 2);
        setPos("DATABASE", startX + colWidth, startY + rowHeight * 3);

        // Column 2 (Rendering Pipeline)
        setPos("TRANSFORM", startX + colWidth * 2, startY + rowHeight);
        setPos("RENDER", startX + colWidth * 2, startY + rowHeight * 2);
    }

    private void setPos(String id, int x, int y) {
        FlowchartNode n = nodeMap.get(id);
        if (n != null) {
            n.setX(x);
            n.setY(y);
        }
    }
}
