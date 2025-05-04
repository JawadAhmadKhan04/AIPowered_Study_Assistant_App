from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain
from langchain_groq import ChatGroq
from dotenv import load_dotenv
import os
import re


class SmartDigest:
    def __init__(self):
        load_dotenv()
        self.groq_api_key = os.getenv("GROQ_API_KEY")
        self.model = ChatGroq(model="Gemma2-9b-It", groq_api_key=self.groq_api_key)
        
        self.summarization_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nSummarize the text based on the given topic. The summary must be in easy words and not more than 50 words."
            "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The key points are...'. "
        "Just return the points directly."
        )
        self.summarization_chain = LLMChain(llm=self.model, prompt=self.summarization_prompt)
        
        self.key_points_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nExtract key points from the text based on the given context. The key points must be in easy words and contain external info as well if it helps in understanding the concept"
            "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The key points are...'. "
        "Just return the points directly in numbered format."
        )
        self.key_points_chain = LLMChain(llm=self.model, prompt=self.key_points_prompt)
        
        self.concept_list_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nExtract a list of concepts from the text based on the given context. The concepts must be in easy words and might contain external info as well if it helps in understanding the concept. "
            "Respond with only the key points as plain, numbered sentences. "
        "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The key points are...'. "
        "Just return the points directly in numbered format."
        )
        self.concept_list_chain = LLMChain(llm=self.model, prompt=self.concept_list_prompt)


    def clean_key_points(self, text):
        text = re.sub(r'\*\*(.*?)\*\*', r'\1', text)  # remove **bold**
        text = re.sub(r'\.{2,}', '.', text)           # replace .. or ... with .
        text = re.sub(r'[-*•]+', '', text)            # remove bullets like *, -, •
        return text.strip()



    def summarize_text(self, context, text):
        summary = self.summarization_chain.run({"context": context, "text": text})
        return self.clean_key_points(summary)
    
    def extract_key_points(self, context, text):
        key_points = self.key_points_chain.run({"context": context, "text": text})
        return self.clean_key_points(key_points)

    def extract_concept_list(self, context, text):
        concept_list = self.concept_list_chain.run({"context": context, "text": text})
        return self.clean_key_points(concept_list)