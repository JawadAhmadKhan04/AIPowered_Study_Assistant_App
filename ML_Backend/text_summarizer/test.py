from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain
from langchain_groq import ChatGroq
from dotenv import load_dotenv
import os

class GroqTextSummarizer:
    def __init__(self):
        load_dotenv()
        self.groq_api_key = os.getenv("GROQ_API_KEY")

        self.model = ChatGroq(model="Gemma2-9b-It", groq_api_key=self.groq_api_key)

        self.summarization_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nSummarize the text based on the given topic. The summary must be in easy words and not more than 50 words."
        )

        self.summarization_chain = LLMChain(llm=self.model, prompt=self.summarization_prompt)

    def summarize_text(self, context, text):
        summary = self.summarization_chain.run({"context": context, "text": text})
        return summary


# Example usage:
if __name__ == "__main__":
    summarizer = GroqTextSummarizer()

    context = "The following text talks about advancements in artificial intelligence, especially in healthcare."
    text = """
    Artificial intelligence (AI) has been rapidly transforming industries, especially healthcare. AI is helping doctors with diagnosing diseases more quickly, predicting patient outcomes, and personalizing treatment plans. However, it also faces challenges related to ethical concerns, such as data privacy and the impact on jobs.
    """

    summary = summarizer.summarize_text(context, text)
    print("Summary:")
    print(summary)
