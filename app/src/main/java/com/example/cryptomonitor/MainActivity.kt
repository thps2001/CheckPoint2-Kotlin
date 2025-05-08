package com.example.cryptomonitor

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.cryptomonitor.service.MercadoBitcoinServiceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Tela principal da app
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configurar a barra de cima (toolbar)
        val minhaToolbar: Toolbar = findViewById(R.id.toolbar_main)
        configurarMinhaToolbar(minhaToolbar)

        // Configurar o botao de atualizar
        val botaoAtualizar: Button = findViewById(R.id.btn_refresh)
        botaoAtualizar.setOnClickListener {
            buscarDadosNaApi()
        }
    }

    // Funcao para arrumar a toolbar
    private fun configurarMinhaToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(getColor(R.color.white)) // Cor do titulo
        supportActionBar?.title = getString(R.string.app_title) // Titulo da app
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.primary)) // Cor de fundo da toolbar
    }

    // Funcao para chamar a API do Mercado Bitcoin
    private fun buscarDadosNaApi() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val servicoApi = MercadoBitcoinServiceFactory().create() //Cria o servico
                val resposta = servicoApi.getTicker() //Pega os dados

                if (resposta.isSuccessful) { // Se deu tudo certo
                    val dadosTicker = resposta.body()

                    // Pegar os TextViews da tela
                    val txtValor: TextView = findViewById(R.id.lbl_value)
                    val txtData: TextView = findViewById(R.id.lbl_date)

                    val ultimoValor = dadosTicker?.ticker?.last?.toDoubleOrNull()
                    if (ultimoValor != null) {
                        // Formatar o valor para mostrar como dinheiro (Reais)
                        val formatadorNumero = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                        txtValor.text = formatadorNumero.format(ultimoValor)
                    }

                    // Formatar a data para mostrar bonitinho
                    val dataTimestamp = dadosTicker?.ticker?.date?.let { Date(it * 1000L) } // Converter o timestamp para data
                    val formatadorData = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    txtData.text = formatadorData.format(dataTimestamp)

                } else {
                    // Se deu erro na resposta da API
                    val msgErro = when (resposta.code()) {
                        400 -> "Ih, deu ruim na requisição (400)"
                        401 -> "Ops, não autorizado (401)"
                        403 -> "Acesso negado (403)"
                        404 -> "Não encontrado (404)"
                        else -> "Erro desconhecido na API"
                    }
                    Toast.makeText(this@MainActivity, msgErro, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                // Se deu erro na chamada da API (tipo sem internet)
                Toast.makeText(this@MainActivity, "Falha ao buscar dados: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
